
import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-email-upload',
  templateUrl: './email-upload.component.html',
})
export class EmailUploadComponent {
  selectedFile: File | null = null;
  result: any = null;

  constructor(private http: HttpClient) {}

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  uploadFile() {
    if (!this.selectedFile) return;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.http.post('/api/email/process', formData).subscribe(response => {
      this.result = response;
    });
  }
}
