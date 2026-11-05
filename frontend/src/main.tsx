import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <main>
      <h1>Gitbook</h1>
      <p>Plataforma de escrita colaborativa.</p>
    </main>
  </StrictMode>,
);
