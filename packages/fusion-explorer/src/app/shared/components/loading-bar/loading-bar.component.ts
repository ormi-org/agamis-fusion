import { AfterViewInit, Component, ElementRef, Input, OnInit } from '@angular/core';
import { Color } from '@shared/constants/assets';
import { Observable, Subject } from 'rxjs';
import { Stage } from './models/stage.model';

const INITIAL_FILL = 0;
const INITIAL_FILL_DURATION = 200;

const DEFAULT_STAGES: Stage[] = [
  {
    fill: 0,
  },
  {
    fill: 80,
  },
  {
    fill: 90,
    autoNext: 100
  },
  {
    fill: 100
  }
]

@Component({
  selector: 'shared-loading-bar',
  templateUrl: './loading-bar.component.html',
  styleUrls: ['./loading-bar.component.scss'],
})
export class LoadingBarComponent implements OnInit, AfterViewInit {
  @Input()
  height!: number;
  @Input()
  width!: number;
  @Input()
  nextStageSignalSubject!: Subject<void>;
  @Input()
  stages: Stage[] = DEFAULT_STAGES;
  private currentStage = -1;
  protected fill: number = INITIAL_FILL;
  protected fillDuration: number = INITIAL_FILL_DURATION;
  @Input()
  fillColor: string = Color.PRIMARY_TWO;
  @Input()
  bgColor: string = Color.PRIMARY_ONE;

  @Input()
  loadingStateObserver!: Observable<boolean>;

  constructor(private host: ElementRef) {}

  ngOnInit(): void {
    if (!this.loadingStateObserver) {
      throw Error('> LoadingBarComponent#ngOnInit >> no loading state observer provided');
    }
    this.loadingStateObserver.subscribe((isLoading) => {
      if (isLoading) {
        this.host.nativeElement.style.visibility = 'visible';
        // trigger initial stage
        this.triggerNextStage();
      } else {
        this.host.nativeElement.style.visibility = 'hidden';
        // reset stage and state
        this.currentStage = -1;
        this.fill = INITIAL_FILL;
        this.fillDuration = INITIAL_FILL_DURATION;
      }
    })
  }

  ngAfterViewInit(): void {
    this.nextStageSignalSubject
    .subscribe(() => {
      this.triggerNextStage();
    });
  }

  private triggerNextStage(): void {
    this.currentStage += 1;
    const nextStage = this.stages[this.currentStage];
    if (nextStage !== undefined) {
      // go to next stage
      this.fill = nextStage.fill;
      this.fillDuration = nextStage.fillDuration || 200;
      // this.cd.detectChanges();
      if (nextStage.autoNext !== undefined) {
        setTimeout(() => {
          this.nextStageSignalSubject.next();
        }, nextStage.autoNext);
      }
    }
  }
}
