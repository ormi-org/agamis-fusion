import { AfterViewInit, Component, Input, OnInit } from '@angular/core';
import { Color } from '@shared/constants/assets';
import { BehaviorSubject } from 'rxjs';
import { Stage } from './models/stage.model';

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
export class LoadingBarComponent implements AfterViewInit {
  @Input()
  height!: number;
  @Input()
  width!: number;
  @Input()
  nextStageSignalSubject!: BehaviorSubject<void>;
  @Input()
  stages: Stage[] = DEFAULT_STAGES;
  private currentStage: number = -1;
  protected fill: number = 0;
  protected fillDuration: number = 200;
  @Input()
  fillColor: string = Color.PRIMARY_TWO;
  @Input()
  bgColor: string = Color.PRIMARY_ONE;

  ngAfterViewInit(): void {
    this.nextStageSignalSubject.subscribe(() => {
      this.currentStage += 1;
      const nextStage = this.stages[this.currentStage];
      if (nextStage !== undefined) {
        this.fill = nextStage.fill;
        this.fillDuration = nextStage.fillDuration || 200;
        if (nextStage.autoNext) {
          setTimeout(() => this.nextStageSignalSubject.next(), nextStage.autoNext);
        }
      }
    });
  }
}
