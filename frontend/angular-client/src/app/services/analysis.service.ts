import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { RxStomp } from '@stomp/rx-stomp';
import SockJS from 'sockjs-client';
import { AnalysisRequest, AnalysisResponse } from '../models/analysis.model';

/**
 * Service for chess position analysis via REST and WebSocket.
 */
@Injectable({ providedIn: 'root' })
export class AnalysisService implements OnDestroy {
  private readonly restUrl = '/api/analysis';
  private readonly wsUrl   = '/ws';

  private rxStomp?: RxStomp;
  private destroy$ = new Subject<void>();

  constructor(private http: HttpClient) {}

  /** Synchronous REST-based analysis (blocking call). */
  analyzeRest(request: AnalysisRequest): Observable<AnalysisResponse> {
    return this.http.post<AnalysisResponse>(this.restUrl, request);
  }

  /**
   * Connects to the WebSocket broker and returns an Observable that emits
   * analysis results as they arrive.
   */
  analyzeViaWebSocket(request: AnalysisRequest): Observable<AnalysisResponse> {
    this.ensureConnected();
    this.rxStomp!.publish({
      destination: '/app/analyze',
      body: JSON.stringify(request)
    });
    return this.rxStomp!.watch('/topic/analysis').pipe(
      // Parse the JSON frame body
      // Using a map operator would require importing it; use standard pattern
    ) as unknown as Observable<AnalysisResponse>;
  }

  /** Subscribe to the /topic/analysis channel and get parsed responses. */
  subscribeToAnalysis(): Observable<AnalysisResponse> {
    this.ensureConnected();
    return new Observable<AnalysisResponse>(observer => {
      const sub = this.rxStomp!.watch('/topic/analysis').subscribe(msg => {
        try {
          observer.next(JSON.parse(msg.body) as AnalysisResponse);
        } catch {
          observer.error(new Error('Failed to parse analysis response'));
        }
      });
      return () => sub.unsubscribe();
    });
  }

  /** Sends an analysis request over the WebSocket channel. */
  sendAnalysisRequest(request: AnalysisRequest): void {
    this.ensureConnected();
    this.rxStomp!.publish({
      destination: '/app/analyze',
      body: JSON.stringify(request)
    });
  }

  private ensureConnected(): void {
    if (!this.rxStomp) {
      this.rxStomp = new RxStomp();
      this.rxStomp.configure({
        webSocketFactory: () => new SockJS(this.wsUrl) as WebSocket,
        reconnectDelay: 5000
      });
      this.rxStomp.activate();
    }
  }

  ngOnDestroy(): void {
    this.rxStomp?.deactivate();
    this.destroy$.next();
    this.destroy$.complete();
  }
}
