import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { fileURLToPath, URL } from 'node:url';
import ts from 'typescript';

const root = fileURLToPath(new URL('../', import.meta.url));
const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');

test('the Swift bridge preserves the public TypeScript API and JavaScript plugin name', () => {
  const declaration = ts.createSourceFile('definitions.ts', read('src/definitions.ts'), ts.ScriptTarget.Latest, true);
  const api = declaration.statements.find(
    (item) => ts.isInterfaceDeclaration(item) && item.name.text === 'AppMetricaPlugin',
  );
  assert.ok(api, 'The public AppMetricaPlugin interface must exist');
  const methods = api.members
    .filter(ts.isMethodSignature)
    .map((item) => item.name.getText(declaration))
    .sort();
  const swift = read('ios/Plugin/Plugin.swift');
  const bridged = [...swift.matchAll(/CAPPluginMethod\(name: "([^"]+)", returnType: (\w+)\)/g)];
  assert.equal(methods.length, 14);
  assert.deepEqual(bridged.map(([, name]) => name).sort(), methods);
  assert.ok(bridged.every(([, , type]) => type === 'CAPPluginReturnPromise'));
  assert.match(swift, /CAPPlugin, CAPBridgedPlugin/);
  const jsName = read('src/index.ts').match(/registerPlugin<AppMetricaPlugin>\('([^']+)'/)?.[1];
  assert.equal(jsName, 'AppMetrica');
  assert.match(swift, new RegExp(`public let jsName = "${jsName}"`));
  assert.match(swift, /public let identifier = "AppMetricaPlugin"/);
});

test('SPM uses the same AppMetrica SDK series as CocoaPods', () => {
  const version = read('CapacitorAppmetricaPlugin.podspec').match(/'AppMetricaAnalytics',\s*'~> ([\d.]+)'/)?.[1];
  assert.equal(version, '5.14.0');
  const manifest = read('Package.swift');
  assert.ok(manifest.includes(`.upToNextMinor(from: "${version}")`));
  assert.match(manifest, /capacitor-swift-pm\.git", from: "8\.0\.0"/);
  for (const product of ['Core', 'Crashes', 'AdSupport', 'WebKit', 'Screenshot', 'IDSync']) {
    assert.ok(manifest.includes(`.product(name: "AppMetrica${product}", package: "appmetrica-sdk-ios")`));
  }
});

test('the published package contains SPM, CocoaPods, Android and JavaScript entry points', () => {
  const packed = JSON.parse(
    execFileSync('npm', ['pack', '--dry-run', '--json', '--ignore-scripts'], {
      cwd: root,
      encoding: 'utf8',
    }),
  );
  const files = new Set(packed[0].files.map(({ path }) => path));
  const pkg = JSON.parse(read('package.json'));
  for (const path of [
    'Package.swift',
    'CapacitorAppmetricaPlugin.podspec',
    'ios/Plugin/Plugin.swift',
    'ios/Plugin/Converter.swift',
    'android/build.gradle',
    'android/src/main/AndroidManifest.xml',
    pkg.main,
    pkg.module,
    pkg.types,
  ])
    assert.ok(files.has(path), `Missing from the npm package: ${path}`);
  assert.ok([...files].some((path) => path.startsWith('android/src/main/') && path.endsWith('.java')));
  assert.equal(files.has('ios/Plugin/Plugin.m'), false);
});

test('the CocoaPods development project has no references to the replaced Objective-C bridge', () => {
  const project = read('ios/Plugin.xcodeproj/project.pbxproj');
  assert.doesNotMatch(project, /Plugin\.m/);
  assert.match(project, /Plugin\.swift in Sources/);
  const manifest = read('Package.swift');
  assert.match(manifest, /path: "ios\/Plugin"/);
  assert.match(manifest, /exclude: \["Info\.plist", "Plugin\.h"\]/);
});
