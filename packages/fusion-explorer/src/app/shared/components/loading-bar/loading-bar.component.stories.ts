import { Meta, moduleMetadata } from '@storybook/angular'
import { SharedModule } from '@shared/shared.module'
import { customViewport } from './.storybook'
import { MINIMAL_VIEWPORTS } from '@storybook/addon-viewport'
import { LoadingBarComponent } from './loading-bar.component'
import { BehaviorSubject } from 'rxjs'

export default {
  title: 'Shared/LoadingBar',
  component: LoadingBarComponent,
  parameters: {
    viewport: {
      viewports: {
        ...customViewport,
        ...MINIMAL_VIEWPORTS
      },
      defaultViewport: 'shared-loading-bar-default'
    }
  },
  decorators: [
    moduleMetadata({
      imports: [SharedModule]
    })
  ]
} as Meta<LoadingBarComponent>

export const Default = {
  render: (args: LoadingBarComponent) => ({
    props: args,
  }),
  args: {
    height: 5,
    width: 240,
    nextStageSignalSubject: new BehaviorSubject<void>(undefined)
  },
}

export const FullAutoNext = {
  render: (args: LoadingBarComponent) => ({
    props: args,
  }),
  args: {
    height: 5,
    width: 240,
    nextStageSignalSubject: new BehaviorSubject<void>(undefined),
    stages: [
      {
        fill: 0,
        autoNext: 100,
      },
      {
        fill: 100
      }
    ]
  },
}

export const AutoNextFourStages = {
  render: (args: LoadingBarComponent) => ({
    props: args,
  }),
  args: {
    height: 5,
    width: 240,
    nextStageSignalSubject: new BehaviorSubject<void>(undefined),
    stages: [
      {
        fill: 0,
        autoNext: 100,
      },
      {
        fill: 20,
        autoNext: 1000,
      },
      {
        fill: 50,
        autoNext: 400,
      },
      {
        fill: 100,
      },
    ]
  },
}

export const AutoNextFourStagesVariableFillDurations = {
  render: (args: LoadingBarComponent) => ({
    props: args,
  }),
  args: {
    height: 5,
    width: 240,
    nextStageSignalSubject: new BehaviorSubject<void>(undefined),
    stages: [
      {
        fill: 0,
        autoNext: 100,
        fillDuration: 100,
      },
      {
        fill: 20,
        autoNext: 300,
        fillDuration: 400,
      },
      {
        fill: 50,
        autoNext: 400,
        fillDuration: 1000,
      },
      {
        fill: 100,
      },
    ]
  },
}

export const AutoNextTenStagesVariableFillDurationsVariantA = {
  render: (args: LoadingBarComponent) => ({
    props: args,
  }),
  args: {
    height: 5,
    width: 240,
    nextStageSignalSubject: new BehaviorSubject<void>(undefined),
    stages: [
      {
        fill: 0,
        autoNext: 100,
        fillDuration: 100,
      },
      {
        fill: 10,
        autoNext: 300,
        fillDuration: 400,
      },
      {
        fill: 20,
        autoNext: 400,
        fillDuration: 1000,
      },
      {
        fill: 40,
        autoNext: 1000,
        fillDuration: 2000,
      },
      {
        fill: 60,
        autoNext: 400,
        fillDuration: 300,
      },
      {
        fill: 70,
        autoNext: 400,
        fillDuration: 100,
      },
      {
        fill: 80,
        autoNext: 200,
        fillDuration: 800,
      },
      {
        fill: 85,
        autoNext: 600,
        fillDuration: 1000,
      },
      {
        fill: 90,
        autoNext: 400,
        fillDuration: 200,
      },
      {
        fill: 100,
        fillDuration: 50,
      },
    ]
  },
}

export const SplashDefault = {
  render: (args: LoadingBarComponent) => ({
    props: args,
  }),
  args: {
    height: 5,
    width: 240,
    nextStageSignalSubject: new BehaviorSubject<void>(undefined),
    stages: [
      {
        fill: 0,
        autoNext: 500
      },
      {
        fill: 13,
        fillDuration: 100,
        autoNext: 400
      },
      {
        fill: 87,
        fillDuration: 10000,
        autoNext: 10000
      },
      {
        fill: 100,
        fillDuration: 300
      }
    ]
  },
}

export const SplashDefaultCut = {
  render: (args: LoadingBarComponent) => ({
    props: args,
  }),
  args: {
    height: 5,
    width: 240,
    nextStageSignalSubject: new BehaviorSubject<void>(undefined),
    stages: [
      {
        fill: 0,
        autoNext: 500
      },
      {
        fill: 13,
        fillDuration: 100,
        autoNext: 400
      },
      {
        fill: 87,
        fillDuration: 10000,
        autoNext: 2000
      },
      {
        fill: 100,
        fillDuration: 300
      }
    ]
  },
}

export const SplashDefaultTimeout = {
  render: (args: LoadingBarComponent) => ({
    props: args,
  }),
  args: {
    height: 5,
    width: 240,
    nextStageSignalSubject: new BehaviorSubject<void>(undefined),
    stages: [
      {
        fill: 0,
        autoNext: 500
      },
      {
        fill: 13,
        fillDuration: 100,
        autoNext: 400
      },
      {
        fill: 87,
        fillDuration: 10000,
        // No autoNext to simulate timeout
      },
      {
        fill: 100,
        fillDuration: 300
      }
    ]
  },
}

export const AlternativeColors = {
  render: (args: LoadingBarComponent) => ({
    props: args,
  }),
  args: {
    height: 5,
    width: 240,
    nextStageSignalSubject: new BehaviorSubject<void>(undefined),
    stages: [
      {
        fill: 0,
        autoNext: 100,
      },
      {
        fill: 100
      }
    ],
    bgColor: '30f251',
    fillColor: 'fa84d3'
  },
}