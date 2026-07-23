import { Routes } from '@angular/router';
import { WorkspacePlaceholder } from './features/workspace-placeholder/workspace-placeholder';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'occupancy' },
  {
    path: 'occupancy',
    component: WorkspacePlaceholder,
    data: {
      eyebrow: 'Operations board',
      title: 'Yard occupancy',
      description:
        'See every track, active reservation, and upcoming movement in one time-bounded operating view.',
      capabilities: [
        'Yard and time filters',
        'Track-by-track reservation windows',
        'Direct links to reservation details',
      ],
    },
  },
  {
    path: 'assignment',
    component: WorkspacePlaceholder,
    data: {
      eyebrow: 'Scheduling desk',
      title: 'Assign a train',
      description:
        'Request explainable track recommendations and commit an assignment only after a fresh conflict check.',
      capabilities: ['Compatibility filtering', 'PriorityQueue ranking', '409 conflict recovery'],
    },
  },
  {
    path: 'trains',
    component: WorkspacePlaceholder,
    data: {
      eyebrow: 'Master data',
      title: 'Trains',
      description:
        'Maintain the train dimensions, service details, and capabilities used by the scheduling engine.',
      capabilities: [
        'Search by train number',
        'Length and capability requirements',
        'Reservation history',
      ],
    },
  },
  {
    path: 'tracks',
    component: WorkspacePlaceholder,
    data: {
      eyebrow: 'Yard configuration',
      title: 'Tracks',
      description:
        'Manage usable length, operation purpose, buffers, capabilities, and operational status for each track.',
      capabilities: [
        'Status and capability controls',
        'Setup and clearance buffers',
        'Future reservation checks',
      ],
    },
  },
  {
    path: 'reservations',
    component: WorkspacePlaceholder,
    data: {
      eyebrow: 'Movement ledger',
      title: 'Reservations',
      description:
        'Review planned and active assignments, controlled status changes, and their complete audit history.',
      capabilities: ['Reservation lifecycle', 'UTC and yard-local time', 'Append-only audit trail'],
    },
  },
  { path: '**', redirectTo: 'occupancy' },
];
