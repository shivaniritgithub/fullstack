import { useState, useEffect } from 'react';
import { fetchTasks } from '../api';

const DEBOUNCE_MS = 300;

export function useTasks(query, status, page, pageSize) {
  const [tasks, setTasks] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [debouncedQuery, setDebouncedQuery] = useState(query);

  // Wait until the user stops typing before using the new search text
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedQuery(query), DEBOUNCE_MS);
    return () => clearTimeout(timer);
  }, [query]);

  useEffect(() => {
    // If a newer request starts, this flag makes us ignore this old response
    let ignore = false;

    setLoading(true);
    setError(null);

    fetchTasks({ query: debouncedQuery, status, page, pageSize })
      .then((data) => {
        if (ignore) return;
        setTasks(data.items);
        setTotal(data.total);
      })
      .catch((err) => {
        if (ignore) return;
        setError(err.message);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [debouncedQuery, status, page, pageSize]);

  return { tasks, total, loading, error };
}