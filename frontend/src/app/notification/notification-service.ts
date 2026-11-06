import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, firstValueFrom } from 'rxjs';
import { NotificationResponse } from './notification.model';

const NOTIFICATIONS_EVENT = 'notification';

@Service()
export class NotificationService {
  private readonly http = inject(HttpClient);

  /**
   * Stellt Server-Sent-Events-Verbindung her und liefert stets die ganze Liste der ungelesenen Benachrichtigungen.
   * @param householdId ID des Haushalts
   */
  streamNotifications(householdId: string): Observable<NotificationResponse[]> {
    return new Observable<NotificationResponse[]>((subscriber) => {
      const source = new EventSource(`/api/households/${householdId}/notifications/stream`);

      source.addEventListener(NOTIFICATIONS_EVENT, (event) => {
        const data = (event as MessageEvent<string>).data;
        subscriber.next(JSON.parse(data) as NotificationResponse[]);
      });

      return () => source.close(); // Verbindung wird beim Verlassen des Dashboards geschlossen
    });
  }

  async readNotifications(householdId: string, notificationIds: string[]): Promise<void> {
    await firstValueFrom(
      this.http.post<void>(`/api/households/${householdId}/notifications/read`, {
        notificationIds,
      }),
    );
  }
}
