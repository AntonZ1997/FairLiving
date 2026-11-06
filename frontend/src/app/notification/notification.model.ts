export type NotificationType = 'info' | 'warning' | 'alert';

export interface NotificationResponse {
  notificationId: string;
  type: NotificationType;
  message: string;
  createdAt: string;
}
