#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

function usage() {
  console.error('Usage: node check-coverage.js <threshold> [lcov_path]');
  process.exit(2);
}

const args = process.argv.slice(2);
if (args.length < 1) usage();
const threshold = Number(args[0]);
if (Number.isNaN(threshold)) usage();

const lcovPath = args[1] || path.join(__dirname, '..', 'coverage', 'bip-app', 'lcov.info');

if (!fs.existsSync(lcovPath)) {
  console.error(`lcov file not found: ${lcovPath}`);
  process.exit(3);
}

const content = fs.readFileSync(lcovPath, 'utf8');

let totalLF = 0; // lines found
let totalLH = 0; // lines hit

const lines = content.split(/\r?\n/);
for (const line of lines) {
  if (line.startsWith('LF:')) {
    totalLF += Number(line.substring(3)) || 0;
  } else if (line.startsWith('LH:')) {
    totalLH += Number(line.substring(3)) || 0;
  }
}

if (totalLF === 0) {
  console.error('No lines found in lcov; cannot compute coverage.');
  process.exit(4);
}

const coverage = (totalLH / totalLF) * 100;
const coverageStr = coverage.toFixed(2);
console.log(`Frontend line coverage: ${coverageStr}% (threshold: ${threshold}%)`);

if (coverage + 1e-9 < threshold) {
  console.error(`Coverage ${coverageStr}% is below threshold ${threshold}%`);
  process.exit(1);
}

process.exit(0);
