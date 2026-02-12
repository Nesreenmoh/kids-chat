
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environments';
import { askRequest, askResponse } from '../models/chat';

import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  askQuestion(body: askRequest): Observable<askResponse> {
    console.log("Body: "+ body.question+" "+ body.useRag);
    return this.http.post<askResponse>(`${this.apiUrl}`, body);
  }
}
