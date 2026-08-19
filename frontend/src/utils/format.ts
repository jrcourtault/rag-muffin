export function formatScore(score?: number): string {
  if (score == null) return '';
  return (score * 100).toFixed(1) + '%';
}
