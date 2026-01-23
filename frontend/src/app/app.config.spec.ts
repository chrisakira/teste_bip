import { TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

import { appConfig } from './app.config';

describe('AppConfig', () => {
  it('should have providers defined', () => {
    expect(appConfig.providers).toBeDefined();
    expect(appConfig.providers.length).toBeGreaterThan(0);
  });

  it('should configure the application correctly', () => {
    TestBed.configureTestingModule({
      providers: appConfig.providers
    });

    // Verify HttpClient is provided
    const httpClient = TestBed.inject(HttpClient, null);
    expect(httpClient).toBeTruthy();

    // Verify Router is provided
    const router = TestBed.inject(Router, null);
    expect(router).toBeTruthy();
  });
});
