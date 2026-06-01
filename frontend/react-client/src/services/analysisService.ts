import axios from 'axios';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { AnalysisRequest, AnalysisResponse } from '../types/chess.types';

const REST_URL = '/api/analysis';
const WS_URL   = '/ws';

/** Analyses a position via REST (synchronous). */
export async function analyzeRest(request: AnalysisRequest): Promise<AnalysisResponse> {
  const res = await axios.post<AnalysisResponse>(REST_URL, request);
  return res.data;
}

/**
 * Connects to the WebSocket broker, sends one analysis request, and resolves
 * with the first response received on /topic/analysis.
 *
 * The connection is closed after the response is received.
 */
export function analyzeViaWebSocket(request: AnalysisRequest): Promise<AnalysisResponse> {
  return new Promise((resolve, reject) => {
    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL) as WebSocket,
      reconnectDelay: 0,
      onConnect: () => {
        client.subscribe('/topic/analysis', frame => {
          try {
            const response = JSON.parse(frame.body) as AnalysisResponse;
            resolve(response);
            client.deactivate();
          } catch {
            reject(new Error('Failed to parse analysis response'));
          }
        });
        client.publish({
          destination: '/app/analyze',
          body: JSON.stringify(request)
        });
      },
      onStompError: frame => {
        reject(new Error('STOMP error: ' + frame.headers.message));
      }
    });
    client.activate();
  });
}
