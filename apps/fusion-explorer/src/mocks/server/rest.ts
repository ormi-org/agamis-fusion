import { rest, setupWorker } from 'msw';
import authRoutes from './routes/auth';
import organizationsRoutes from './routes/organizations';
import profilesRoutes from './routes/profiles';

export const mocks = [...authRoutes, ...organizationsRoutes, ...profilesRoutes];

const worker = setupWorker(...mocks);

worker.start({
  onUnhandledRequest: 'bypass',
});

export { rest, worker };
