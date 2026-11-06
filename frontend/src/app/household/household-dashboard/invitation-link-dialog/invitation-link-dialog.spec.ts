import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InvitationLinkDialog } from './invitation-link-dialog';

describe('InvitationLinkDialog', () => {
  let component: InvitationLinkDialog;
  let fixture: ComponentFixture<InvitationLinkDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvitationLinkDialog],
    }).compileComponents();

    fixture = TestBed.createComponent(InvitationLinkDialog);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
