import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-top-nav-bar',
  templateUrl: './top-nav-bar.component.html',
  styles: [``]
})
export class TopNavBarComponent implements OnInit {

  private setFont = 13;

  constructor() { }

  ngOnInit() {


  }

  toggleSystemFont(action){


    if (action == 'plus') {

      this.setFont += 1;

    }else if (action == 'minus') {

      this.setFont -= 1;
    }else {
      this.setFont = 13;
    }

    document.body.style.fontSize = `${this.setFont}px`;


    //alert(document.body.style.fontSize);

  }

}
