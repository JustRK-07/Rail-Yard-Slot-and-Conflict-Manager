import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

interface NavigationItem {
  readonly index: string;
  readonly label: string;
  readonly route: string;
}

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly navigation: readonly NavigationItem[] = [
    { index: '01', label: 'Yard occupancy', route: '/occupancy' },
    { index: '02', label: 'Assign a train', route: '/assignment' },
    { index: '03', label: 'Trains', route: '/trains' },
    { index: '04', label: 'Tracks', route: '/tracks' },
    { index: '05', label: 'Reservations', route: '/reservations' },
  ];
}
