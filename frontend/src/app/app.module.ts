import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

// Angular Material modules
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatButtonModule } from '@angular/material/button';       // ✅ Add this
import { MatCardModule } from '@angular/material/card';           // ✅ Add this
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { AppComponent } from './app.component';
import { EmailUploadComponent } from './email-upload/email-upload.component';

@NgModule({
  declarations: [
    AppComponent,
    EmailUploadComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    // Material UI modules
    MatToolbarModule,
    MatSlideToggleModule,
    MatButtonModule,   // ✅ Required for <button mat-raised-button>
    MatCardModule,     // ✅ Required for <mat-card>
    MatSnackBarModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
