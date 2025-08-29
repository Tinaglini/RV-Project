import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DescartavelComponent } from './components/descartavel.component';
import { DescartavelModelComponent } from './models/descartavel-model.component';
import { DescartavelServiceComponent } from './services/descartavel-service.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, DescartavelComponent, DescartavelModelComponent, DescartavelServiceComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('RV-Digital-Front');
}
