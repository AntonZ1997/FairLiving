export function extractInvitationId(value: string): string {
  const trimmed = value.trim();

  try {
    const segments = new URL(trimmed).pathname.split('/').filter((segment) => segment.length > 0);
    return segments.at(-1) ?? '';
  } catch {
    return trimmed;
  }
}
