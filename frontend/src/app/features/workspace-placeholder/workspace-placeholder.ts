import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

interface WorkspaceData {
  readonly eyebrow: string;
  readonly title: string;
  readonly description: string;
  readonly capabilities: readonly string[];
}

@Component({
  selector: 'app-workspace-placeholder',
  templateUrl: './workspace-placeholder.html',
  styleUrl: './workspace-placeholder.scss',
})
export class WorkspacePlaceholder {
  private readonly route = inject(ActivatedRoute);

  protected readonly page = this.route.snapshot.data as WorkspaceData;
}
