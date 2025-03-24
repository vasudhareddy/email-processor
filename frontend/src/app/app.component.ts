
import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'Email Processor';
  isDark = false;

  toggleTheme() {
    this.isDark = !this.isDark;
    const body = document.body;
    if (this.isDark) {
      body.classList.add('dark-theme');
      body.classList.remove('light-theme');
    } else {
      body.classList.add('light-theme');
      body.classList.remove('dark-theme');
    }
  }

  ngOnInit() {
    document.body.classList.add('light-theme');
  }
}
