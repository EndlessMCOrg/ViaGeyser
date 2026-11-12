#!/usr/bin/env python3
"""
Legacy (Bedrock 1.21.0 - 1.21.132) support helper for the ViaGeyser fork.

Subcommands:
  fetch  Download the historical Bedrock data files from the GeyserMC history / old fork
  push   Create the feature branch on EndlessMCOrg/ViaGeyser through the GitHub API
  all    fetch + push

The script is meant to be run from a checkout of the fork. It does not require a working
`git` remote for GitHub: all writes go through the REST API.
"""
import base64
import json
import os
import sys
import time
import urllib.error
import urllib.request

TOKEN = os.environ.get('GITHUB_TOKEN') or open('/root/.ghtoken').read().strip()
API = 'https://api.github.com'
NEW_REPO = 'EndlessMCOrg/ViaGeyser'
OLD_REPO = 'OutHimic/ViaGeyser'
UPSTREAM = 'GeyserMC/Geyser'
BRANCH = 'feature/legacy-1.21'

ROOT = os.path.dirname(os.path.abspath(__file__))
while not os.path.isdir(os.path.join(ROOT, 'core', 'src', 'main', 'resources', 'bedrock')):
    parent = os.path.dirname(ROOT)
    if parent == ROOT:
        raise SystemExit('Could not locate the ViaGeyser checkout')
    ROOT = parent

RES = os.path.join(ROOT, 'core', 'src', 'main', 'resources', 'bedrock')

# key -> (source, ref) for GeyserMC/Geyser or OutHimic/ViaGeyser
DATA = {
    '1_21_0':   ('upstream', '4eb34387cf'),
    '1_21_20':  ('upstream', '4eb34387cf'),
    '1_21_30':  ('upstream', '4f32fb7174'),
    '1_21_40':  ('upstream', '9e276c1077'),
    '1_21_50':  ('upstream', '9e276c1077'),
    '1_21_60':  ('upstream', '4180c13f72'),
    '1_21_70':  ('upstream', '24f774e767'),
    '1_21_80':  ('upstream', '871390b803'),
    '1_21_90':  ('upstream', '871390b803'),
    '1_21_93':  ('upstream', 'd2dd949eeb'),
    '1_21_100': ('upstream', 'd2dd949eeb'),
    '1_21_110': ('upstream', 'd2dd949eeb'),
    '1_21_120': ('old', 'master'),
    '1_21_130': ('upstream', 'b728af2e89'),
}

BLOCK_PALETTES = [
    '1_21_0', '1_21_20', '1_21_30', '1_21_40', '1_21_50', '1_21_60', '1_21_70',
    '1_21_80', '1_21_90', '1_21_100', '1_21_110', '1_21_130',
]
CREATIVE = [
    '1_21_0', '1_21_20', '1_21_30', '1_21_40', '1_21_50', '1_21_60', '1_21_70',
    '1_21_80', '1_21_90', '1_21_93', '1_21_100', '1_21_110', '1_21_120', '1_21_130',
]
RUNTIME = CREATIVE
TAGS = [
    '1_21_40', '1_21_50', '1_21_60', '1_21_70', '1_21_90', '1_21_100',
    '1_21_110', '1_21_120', '1_21_130',
]
# Tags for 1.21.90 were added later than the palette file, so they come from another commit.
TAG_SOURCES = {
    '1_21_90': ('upstream', 'd2dd949eeb'),
}
# target key -> (source, ref, file name in that source)
COMPONENTS = {
    '1_21_60': ('upstream', '4180c13f72', 'item_components.nbt'),
    '1_21_70': ('upstream', '24f774e767', 'item_components.nbt'),
    '1_21_80': ('upstream', '871390b803', 'item_components.nbt'),
    '1_21_90': ('upstream', '871390b803', 'item_components.nbt'),
    '1_21_93': ('upstream', 'd2dd949eeb', 'item_components.nbt'),
    '1_21_100': ('old', 'master', 'item_components.1_21_100.nbt'),
    '1_21_110': ('old', 'master', 'item_components.1_21_110.nbt'),
    '1_21_120': ('old', 'master', 'item_components.1_21_120.nbt'),
    '1_21_130': ('upstream', 'b728af2e89', 'item_components.1_21_130.nbt'),
}

JAVA_FILES = [
    'core/src/main/java/org/geysermc/geyser/network/bedrock/GameProtocol.java',
    'core/src/main/java/org/geysermc/geyser/network/bedrock/CodecProcessor.java',
    'core/src/main/java/org/geysermc/geyser/registry/populator/BlockRegistryPopulator.java',
    'core/src/main/java/org/geysermc/geyser/registry/populator/ItemRegistryPopulator.java',
    'core/src/main/java/org/geysermc/geyser/registry/populator/TagRegistryPopulator.java',
    'core/src/main/java/org/geysermc/geyser/registry/populator/CreativeItemRegistryPopulator.java',
    'core/src/main/java/org/geysermc/geyser/registry/populator/conversion/WildernessBoundConverter.java',
    'core/src/main/java/org/geysermc/geyser/registry/populator/conversion/LegacyBlockConverter.java',
    'core/src/main/java/org/geysermc/geyser/registry/populator/conversion/LegacyItemFallbacks.java',
    'tools/legacy-support/legacy_support.py',
]


def request(method, url, body=None, raw=False, retries=4):
    data = None
    headers = {'Authorization': 'token ' + TOKEN, 'User-Agent': 'via-geyser-legacy'}
    if body is not None:
        data = json.dumps(body).encode()
        headers['Content-Type'] = 'application/json'
    headers['Accept'] = 'application/vnd.github.raw' if raw else 'application/vnd.github+json'
    for attempt in range(retries):
        req = urllib.request.Request(url, data=data, headers=headers, method=method)
        try:
            with urllib.request.urlopen(req, timeout=120) as resp:
                return resp.read(), resp.status
        except urllib.error.HTTPError as e:
            payload = e.read()
            if e.code in (403, 429, 500, 502, 503) and attempt < retries - 1:
                time.sleep(2 + attempt * 2)
                continue
            raise RuntimeError(f'{method} {url} -> {e.code}: {payload[:300]}')
        except Exception:
            if attempt < retries - 1:
                time.sleep(2 + attempt * 2)
                continue
            raise
    raise RuntimeError('unreachable')


def download(repo, ref, remote_path, local_path):
    url = f'{API}/repos/{repo}/contents/{remote_path}?ref={ref}'
    data, _ = request('GET', url, raw=True)
    if len(data) < 500:
        raise RuntimeError(f'Download too small ({len(data)} bytes): {repo}/{remote_path}@{ref}')
    os.makedirs(os.path.dirname(local_path), exist_ok=True)
    with open(local_path, 'wb') as f:
        f.write(data)
    print(f'  {os.path.basename(local_path):52s} {len(data):>9,d} bytes  <- {repo}@{ref}')


def source_of(key):
    kind, ref = DATA[key]
    return (UPSTREAM if kind == 'upstream' else OLD_REPO), ref


def fetch():
    for key in BLOCK_PALETTES:
        repo, ref = source_of(key)
        download(repo, ref, f'core/src/main/resources/bedrock/block_palette.{key}.nbt',
                 os.path.join(RES, f'block_palette.{key}.nbt'))
    for key in CREATIVE:
        repo, ref = source_of(key)
        download(repo, ref, f'core/src/main/resources/bedrock/creative_items.{key}.json',
                 os.path.join(RES, f'creative_items.{key}.json'))
    for key in RUNTIME:
        repo, ref = source_of(key)
        download(repo, ref, f'core/src/main/resources/bedrock/runtime_item_states.{key}.json',
                 os.path.join(RES, f'runtime_item_states.{key}.json'))
    for key in TAGS:
        kind, ref = TAG_SOURCES.get(key) or DATA[key]
        repo = UPSTREAM if kind == 'upstream' else OLD_REPO
        download(repo, ref, f'core/src/main/resources/bedrock/item_tags.{key}.json',
                 os.path.join(RES, f'item_tags.{key}.json'))
    for key, (kind, ref, source_name) in COMPONENTS.items():
        repo = UPSTREAM if kind == 'upstream' else OLD_REPO
        download(repo, ref, f'core/src/main/resources/bedrock/{source_name}',
                 os.path.join(RES, f'item_components.{key}.nbt'))


def commit_files():
    files = list(JAVA_FILES)
    for key in BLOCK_PALETTES:
        files.append(f'core/src/main/resources/bedrock/block_palette.{key}.nbt')
    for key in CREATIVE:
        files.append(f'core/src/main/resources/bedrock/creative_items.{key}.json')
    for key in RUNTIME:
        files.append(f'core/src/main/resources/bedrock/runtime_item_states.{key}.json')
    for key in TAGS:
        files.append(f'core/src/main/resources/bedrock/item_tags.{key}.json')
    for key in COMPONENTS:
        files.append(f'core/src/main/resources/bedrock/item_components.{key}.nbt')
    return files


def push(message):
    ref, _ = request('GET', f'{API}/repos/{NEW_REPO}/git/ref/heads/master')
    master = json.loads(ref)['object']['sha']
    commit_data, _ = request('GET', f'{API}/repos/{NEW_REPO}/git/commits/{master}')
    base_tree = json.loads(commit_data)['tree']['sha']
    print(f'master={master} tree={base_tree}')

    tree_entries = []
    for path in commit_files():
        local = os.path.join(ROOT, path)
        if not os.path.isfile(local):
            raise RuntimeError('Missing file (run "fetch" first): ' + path)
        with open(local, 'rb') as f:
            content = f.read()
        body = {'content': base64.b64encode(content).decode(), 'encoding': 'base64'}
        blob, _ = request('POST', f'{API}/repos/{NEW_REPO}/git/blobs', body)
        tree_entries.append({'path': path, 'mode': '100644', 'type': 'blob',
                             'sha': json.loads(blob)['sha']})
        print(f'  blob {path} ({len(content):,d} bytes)')

    tree, _ = request('POST', f'{API}/repos/{NEW_REPO}/git/trees',
                      {'base_tree': base_tree, 'tree': tree_entries})
    commit_body = {'message': message, 'tree': json.loads(tree)['sha'], 'parents': [master]}
    created, _ = request('POST', f'{API}/repos/{NEW_REPO}/git/commits', commit_body)
    commit_sha = json.loads(created)['sha']
    print(f'commit={commit_sha}')

    try:
        request('GET', f'{API}/repos/{NEW_REPO}/git/ref/heads/{BRANCH}')
        request('PATCH', f'{API}/repos/{NEW_REPO}/git/refs/heads/{BRANCH}',
                {'sha': commit_sha, 'force': False})
    except RuntimeError as e:
        if '404' not in str(e):
            raise
        request('POST', f'{API}/repos/{NEW_REPO}/git/refs',
                {'ref': f'refs/heads/{BRANCH}', 'sha': commit_sha})

    print(f'Branch {BRANCH} now points at {commit_sha}')
    print(f'https://github.com/{NEW_REPO}/tree/{BRANCH}')


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return 1
    cmd = sys.argv[1]
    if cmd in ('fetch', 'all'):
        print('Fetching historical 1.21.x data files...')
        fetch()
    if cmd in ('push', 'all'):
        push(
            'Support Bedrock 1.21.0 through 1.21.132\n\n'
            'Register Bedrock codecs v685-v898 and add the matching block palettes, creative items, '
            'runtime item states, item tags and item components. Legacy palettes downgrade newer '
            'blocks and items on a best-effort basis so older clients can still join.'
        )
    return 0


if __name__ == '__main__':
    sys.exit(main())