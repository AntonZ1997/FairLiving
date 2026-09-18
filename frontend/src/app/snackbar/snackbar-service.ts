import { inject, Service } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Service()
export class SnackbarService {

  private readonly snackBar = inject(MatSnackBar);

  success(message: string) : void{
    this.snackBar.open(message, 'OK', {panelClass: 'snackbar-success'})
  }

  error(message: string) : void{
    this.snackBar.open(message, 'OK', {panelClass: 'snackbar-error'})
  }
}
