import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

test('renders main application', () => {
  render(<App />);
  const welcomeText = screen.getByText('Bienvenue dans MAGSAV-3.0');
  expect(welcomeText).toBeInTheDocument();
});