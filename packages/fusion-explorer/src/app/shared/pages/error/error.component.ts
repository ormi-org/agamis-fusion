import { Component, OnInit } from '@angular/core'
import { ActivatedRoute, Router } from '@angular/router'
import { Icon } from '@shared/constants/assets'

type ErrorData = {
  code: number,
  title: string
  text: string
}

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.scss'],
})
export class ErrorComponent implements OnInit {
  protected fusionIcon: Icon = Icon.AGAMIS_FUSION_LOGO
  protected code!: number
  protected title!: string
  protected text!: string

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router
  ) {
    const state = <ErrorData>this.router.getCurrentNavigation()?.extras.state
    if (state !== undefined) {
      const { code, title, text } = state
      this.code = code
      this.title = title
      this.text = text
    }
  }

  ngOnInit(): void {
      this.activatedRoute.data.subscribe(data => {
        const { code, title, text } = <ErrorData>data
        this.code ??= code
        this.title ??= title
        this.text ??= text
      })
  }
}
