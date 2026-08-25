import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { RagResponse } from '../../models/rag-response';
import { ChatRequest } from '../../models/ask-request';

@Injectable({
  providedIn: 'root',
})
export class ChatService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080/api/ai';

  //Simple Chat
  simpleChat(question: string): Observable<any> {

    console.log(question);
    const request: ChatRequest =
    {
      message: question
    };

    return this.http.post<any>(`${this.apiUrl}/chat`, request);
  }

  //Simple Stream Chat
  //
  // This method calls the '/chat/stream' backend API and
  // continuously receives the AI response as SSE chunks.
  //
  // Instead of waiting for the complete answer, every chunk
  // is passed to the onChunk callback.
  //
  // Parameters:
  //
  // question   -> User's question
  //
  // onChunk    -> Callback function called whenever a new
  //               chunk of AI response is received.
  //
  // onComplete -> Optional callback called when the entire
  //               streaming response is finished.
  //
  // onError    -> Optional callback called if an error occurs.
  //
  // =========================================================
  simpleChatStream(question: string,
    
    // Function that receives every AI response chunk
    onChunk: (chunk: string) => void,

    // Function called after streaming is completely finished
    onComplete?: () => void,

    // Function called if any error occurs
    onError?: (error: any) => void
  ): void {

    // =======================================================
    // STEP 1: Build the backend streaming API URL
    // =======================================================
    //
    // Example:
    // http://localhost:8080/api/ai/chat/stream
    //
    const url = `${this.apiUrl}/chat/stream`;

    // =======================================================
    // STEP 2: Send HTTP request to backend
    // =======================================================
    //
    // fetch() returns a Promise containing the HTTP response.
    //
    // We use fetch() instead of HttpClient here because we
    // need direct access to response.body as a ReadableStream.
    //
    fetch(url, {

      // Backend endpoint expects POST request
      method: 'POST',

      // HTTP HEADERS
      headers: {
        // We are sending JSON in the request body
        'Content-Type': 'application/json',

        // We are telling the backend that we expect
        // Server-Sent Events (SSE) as the response.
        'Accept': 'text/event-stream'
      },

      // -------------------------------------------------------
      // REQUEST BODY
      // -------------------------------------------------------
      //
      // Convert JavaScript object into JSON string.
      //
      // Example:
      //
      // {
      //     "message": "What is Spring Boot?"
      // }
      //
      body: JSON.stringify({message: question})

    })
      // =====================================================
      // STEP 3: Handle HTTP response
      // =====================================================
      //
      // fetch() resolves the Promise when the HTTP response
      // is received.
      //
      // The response body itself is still being streamed.
      //
      .then(async response => {

        // ===================================================
        // STEP 3.1: Check HTTP status
        // ===================================================
        //
        // response.ok is true for successful HTTP responses
        // such as 200, 201, etc.
        //
        // If backend returns 4xx or 5xx, throw an error.
        //
        if (!response.ok) {

          throw new Error(`HTTP ${response.status}`);
        }


        // ===================================================
        // STEP 3.2: Check whether response body exists
        // ===================================================
        //
        // For streaming, we need response.body.
        //
        // response.body is a ReadableStream.
        //
        // If it doesn't exist, we cannot read the response
        // chunk by chunk.
        //
        if (!response.body) {
          throw new Error('Streaming is not supported by this response.');
        }

        // ===================================================
        // STEP 4: Get a reader for the response stream
        // ===================================================
        //
        // response.body is a ReadableStream.
        //
        // getReader() gives us an object through which we
        // can continuously read incoming data.
        //
        const reader = response.body.getReader();

        // ===================================================
        // STEP 5: Create TextDecoder
        // ===================================================
        //
        // Data received from the network comes as bytes.
        //
        // TextDecoder converts those bytes into normal
        // JavaScript strings.
        //
        // Example:
        //
        // Network bytes
        //      ↓
        // TextDecoder
        //      ↓
        // "event: chunk\ndata: Spring"
        //
        const decoder = new TextDecoder();

        // ===================================================
        // STEP 6: Create temporary buffer
        // ===================================================
        //
        // IMPORTANT:
        //
        // One reader.read() does NOT necessarily contain
        // one complete SSE event.
        //
        // An SSE event may be split across multiple reads.
        //
        // Therefore we keep incomplete data in this buffer
        // until we receive the complete event.
        //
        let buffer = '';

        // ===================================================
        // STEP 7: Continuously read the streaming response
        // ===================================================
        //
        // This loop continues until:
        //
        // done === true
        //
        while (true) {

          // -------------------------------------------------
          // Read the next piece of data from the stream
          // -------------------------------------------------
          //
          // value -> bytes received from backend
          //
          // done  -> tells us whether the stream is finished
          //
          const { value, done } = await reader.read();

          // -------------------------------------------------
          // STEP 7.1: Check if streaming is finished
          // -------------------------------------------------
          //
          // done === true means backend has closed the
          // response stream.
          //
          if (done) {
            break;
          }

          // =================================================
          // STEP 8: Convert received bytes into text
          // =================================================
          //
          // decoder.decode() converts network bytes into
          // a JavaScript string.
          //
          // { stream: true } tells TextDecoder that more
          // bytes may arrive in the next read().
          //
          buffer += decoder.decode(
            value,
            {
              stream: true
            }
          );

          // =================================================
          // STEP 9: Normalize line endings
          // =================================================
          //
          // Windows commonly uses:
          //
          //     \r\n
          //
          // Linux/Unix commonly uses:
          //
          //     \n
          //
          // We convert everything to \n so that SSE parsing
          // works consistently.
          //
          buffer = buffer.replace(/\r\n/g, '\n');

          // =================================================
          // STEP 10: Split complete SSE events
          // =================================================
          //
          // SSE events are separated by a blank line:
          //
          //     \n\n
          //
          // Example:
          //
          // event: chunk
          // data: Spring
          //
          // event: chunk
          // data: Boot
          //
          // split('\n\n') gives us individual events.
          //
          const events = buffer.split('\n\n');

          // =================================================
          // STEP 11: Keep the last item in the buffer
          // =================================================
          //
          // IMPORTANT:
          //
          // The last item may be incomplete.
          //
          // Example:
          //
          // event: chunk
          // data: Spring
          //
          // event: chunk
          // data: Bo
          //
          // "Bo" may not be complete yet.
          //
          // Therefore we remove the last item from events
          // and keep it in buffer.
          //
          buffer = events.pop() ?? '';

          // =================================================
          // STEP 12: Process all complete SSE events
          // =================================================
          //
          // Every complete event is passed to
          // processSimpleSseEvent().
          //
          for (const event of events) {

            // Ignore empty events
            if (event.trim()) {

              // Parse the SSE event and extract the actual
              // AI response text.
              //
              // onChunk is passed so that the parser can
              // notify the component whenever a chunk arrives.
              //
              this.processSimpleSseEvent(event, onChunk);
            }
          }
        }

        // ===================================================
        // STEP 13: Process any remaining data
        // ===================================================
        //
        // After the while loop finishes, there might still
        // be some data left inside the buffer.
        //
        // Normalize line endings one final time.
        //
        buffer = buffer.replace(/\r\n/g, '\n');

        // If buffer contains something, process it.
        //
        if (buffer.trim()) {
          this.processSimpleSseEvent(buffer, onChunk);
        }

        // ===================================================
        // STEP 14: Streaming completed
        // ===================================================
        //
        // At this point:
        //
        // - All chunks have been received
        // - All chunks have been sent to onChunk()
        // - The backend stream is finished
        //
        console.log('Simple Chat streaming completed');

        // Call the completion callback that was provided
        // by the Angular component.
        //
        // Example:
        //
        // () => {
        //     this.loading.set(false);
        // }
        //
        onComplete?.();

      })

      // =====================================================
      // STEP 15: Handle errors
      // =====================================================
      //
      // Any error thrown inside the fetch/stream processing
      // chain comes here.
      //
      .catch(error => {

        // Log the error for debugging
        console.error('Simple Chat streaming error:', error);

        // Call the error callback provided by the component.
        //
        // Example:
        //
        // (error) => {
        //     this.answer.set('Something went wrong...');
        //     this.loading.set(false);
        // }
        //
        onError?.(error);
      });
  }

  // =========================================================
  // SSE EVENT PROCESSOR
  // =========================================================
  //
  // This method receives ONE COMPLETE SSE EVENT.
  //
  // Example:
  //
  // event: chunk
  // data: Spring
  //
  // Its job is:
  //
  // 1. Read the event type
  // 2. Extract the data
  // 3. If it is a "chunk" event,
  //    send the data to onChunk()
  //
  // =========================================================
  private processSimpleSseEvent(event: string,

    // Callback function received from the component.
    //
    // This function will be called whenever we extract
    // an actual AI response chunk.
    onChunk: (chunk: string) => void): void {

    // =======================================================
    // STEP 1: Print raw SSE event
    // =======================================================
    //
    // Useful for debugging.
    //
    // Example:
    //
    // event: chunk
    // data: Spring
    //
    console.log('RAW SIMPLE SSE EVENT:', event);

    // =======================================================
    // STEP 2: Split event into individual lines
    // =======================================================
    //
    // Example:
    //
    // "event: chunk\ndata: Spring"
    //
    // becomes:
    //
    // [
    //     "event: chunk",
    //     "data: Spring"
    // ]
    //
    const lines = event.split('\n');

    // =======================================================
    // STEP 3: Variable to store event type
    // =======================================================
    //
    // Initially we don't know what type of SSE event
    // we received.
    //
    // Later it may become:
    //
    //     "chunk"
    //
    let eventType = '';

    // =======================================================
    // STEP 4: Store all data lines
    // =======================================================
    //
    // SSE allows multiple data: lines.
    //
    // Therefore we store them in an array first and
    // combine them later.
    //
    const dataLines: string[] = [];

    // =======================================================
    // STEP 5: Process every line
    // =======================================================
    //
    for (const line of lines) {

      // -----------------------------------------------------
      // Check whether this line contains event type
      // -----------------------------------------------------
      //
      // Example:
      //
      // "event: chunk"
      //
      if (line.startsWith('event:')) {

        // Remove "event:" from the beginning.
        //
        // substring(6):
        //
        // "event: chunk"
        //       ↓
        // " chunk"
        //
        // trim():
        //
        // " chunk"
        //       ↓
        // "chunk"
        //
        eventType = line.substring(6).trim();
      }

      // -----------------------------------------------------
      // Check whether this line contains data
      // -----------------------------------------------------
      //
      // Example:
      //
      // "data: Spring"
      //
      else if (line.startsWith('data:')) {

        // Remove "data:" from the beginning.
        //
        // IMPORTANT:
        //
        // We intentionally don't call trim() here because
        // spaces can be part of the LLM response.
        //
        // Example:
        //
        // "data:  Boot"
        //
        // should preserve the spaces before "Boot".
        //
        dataLines.push(line.substring(5));
      }
    }

    // =======================================================
    // STEP 6: Combine all data lines
    // =======================================================
    //
    // If multiple data lines exist, join them with newline.
    //
    // Example:
    //
    // [
    //     "Spring",
    //     "Boot"
    // ]
    //
    // becomes:
    //
    // "Spring\nBoot"
    //
    const data = dataLines.join('\n');

    // =======================================================
    // STEP 7: Debug logs
    // =======================================================
    //
    // Shows what type of SSE event we received.
    //
    console.log('SSE TYPE:', eventType);

    // Shows the actual extracted data.
    //
    console.log('SSE DATA:', data);

    // =======================================================
    // STEP 8: Handle "chunk" event
    // =======================================================
    //
    // Our backend creates events like:
    //
    // event: chunk
    // data: Spring
    //
    // Therefore eventType will be:
    //
    //     "chunk"
    //
    if (eventType === 'chunk') {

      // Send the extracted text to the component.
      //
      // This executes the callback that was originally
      // passed from SimpleChatComponent.
      //
      // Example:
      //
      // onChunk("Spring")
      //
      // which eventually executes:
      //
      // this.answer.update(
      //     current => current + chunk
      // );
      //
      onChunk(data);

      // We have already handled this event,
      // so exit the method.
      return;
    }


    // =======================================================
    // STEP 9: Handle plain SSE data
    // =======================================================
    //
    // This is a fallback case.
    //
    // If backend sends:
    //
    // data: Spring
    //
    // without:
    //
    // event: chunk
    //
    // then eventType will remain empty.
    //
    if (!eventType && data) {

      // Still send the data to the component.
      //
      onChunk(data);

      // Event has been handled.
      return;
    }
  }

}