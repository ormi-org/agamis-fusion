import { Component, OnInit } from '@angular/core';
import { HeaderTitleItem } from '@core/models/header-title-item.model';
import { selectOrganization } from '@explorer/states/explorer-state/explorer-state.selectors';
import { Store } from '@ngrx/store';
import { Icon } from '@shared/constants/assets';

@Component({
  selector: 'shared-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit {
  protected fusionIcon: Icon = Icon.AGAMIS_FUSION_LOGO;
  protected title: HeaderTitleItem[] = [
    {
      text: 'fusion',
      style: {
        weight: 800
      }
    },
    {
      text: '',
      style: {
          weight: 800
      }
    }
  ];

  constructor(private readonly store: Store) {}

  ngOnInit(): void {
    this.store.select(selectOrganization).subscribe((org) => {
      this.title[1].text = org?.label || '';
    });
  }
}
