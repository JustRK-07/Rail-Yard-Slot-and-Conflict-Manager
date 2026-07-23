import { ActivatedRoute } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { WorkspacePlaceholder } from './workspace-placeholder';

describe('WorkspacePlaceholder', () => {
  it('renders route-provided module details', async () => {
    await TestBed.configureTestingModule({
      imports: [WorkspacePlaceholder],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: {
                eyebrow: 'Scheduling desk',
                title: 'Assign a train',
                description: 'Choose a conflict-free track.',
                capabilities: ['Compatibility filtering'],
              },
            },
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(WorkspacePlaceholder);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('h1')?.textContent).toContain('Assign a train');
    expect(compiled.querySelector('li')?.textContent).toContain('Compatibility filtering');
  });
});
