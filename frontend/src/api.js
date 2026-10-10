const API_BASE = '/api';

export async function fetchTasks({ query = '', status = '', page = 1, pageSize = 10 }) {
  const params = new URLSearchParams();
  if (query) params.set('q', query);
  if (status) params.set('status', status);
  params.set('page', String(page));
  params.set('pageSize', String(pageSize));

  const response = await fetch(`${API_BASE}/tasks?${params.toString()}`);

  if (!response.ok) {
    // Show the server's own message when it sends one (e.g. invalid status)
    let message = `Request failed: ${response.status}`;
    try {
      const body = await response.json();
      if (body && body.error) message = body.error;
    } catch (e) {
      // response was not JSON, keep the default message
    }
    throw new Error(message);
  }

  return response.json();
}