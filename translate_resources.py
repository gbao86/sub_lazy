import os
import xml.etree.ElementTree as ET
import urllib.request
import urllib.parse
import json
import re
import time

# Config
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MASTER_STRINGS_PATH = os.path.join(BASE_DIR, 'app', 'src', 'main', 'res', 'values', 'strings.xml')
RES_DIR = os.path.join(BASE_DIR, 'app', 'src', 'main', 'res')

# Language mapping: Google Translate Code -> Android folder suffix
LANGUAGES = {
    'vi': 'vi',
    'zh-CN': 'zh',
    'th': 'th',
    'es': 'es',
    'ja': 'ja',
    'ko': 'ko',
    'fr': 'fr'
}

def clean_escapes(translated):
    # Android strings require single quotes and double quotes to be escaped: \' and \"
    # But only if they aren't already escaped
    processed = []
    i = 0
    while i < len(translated):
        if translated[i] == "'":
            if i == 0 or translated[i-1] != '\\':
                processed.append("\\'")
            else:
                processed.append("'")
        elif translated[i] == '"':
            if i == 0 or translated[i-1] != '\\':
                processed.append('\\"')
            else:
                processed.append('"')
        else:
            processed.append(translated[i])
        i += 1
    return "".join(processed)

def translate_batch(batch_texts, target_lang):
    """Translates a batch of texts in a single HTTP request using newline delimiter."""
    if not batch_texts:
        return []

    # Map placeholders to protect them in each string
    protected_texts = []
    placeholders_map = []
    escapes_map = []

    for text in batch_texts:
        if not text or text.strip() == "":
            protected_texts.append("")
            placeholders_map.append([])
            escapes_map.append([])
            continue

        placeholders = []
        # Protect format specifiers (e.g. %1$s, %d, %s)
        pattern = re.compile(r'(%[0-9]+\$[a-zA-Z]|%[a-zA-Z])')
        def repl(match):
            placeholder = match.group(0)
            index = len(placeholders)
            placeholders.append(placeholder)
            return f" [{index}] "
        temp_text = pattern.sub(repl, text)

        # Protect escapes (\n, \', \")
        escapes = []
        def repl_escape(match):
            escape = match.group(0)
            index = len(escapes)
            escapes.append(escape)
            return f" [E{index}] "
        temp_text = re.sub(r'(\\\'|\\\"|\\n)', repl_escape, temp_text)

        protected_texts.append(temp_text)
        placeholders_map.append(placeholders)
        escapes_map.append(escapes)

    # Join the texts with a special separator
    # Google Translate usually preserves '|||' or '___' separators
    separator = " ||| "
    joined_text = separator.join(protected_texts)

    try:
        url = f"https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl={target_lang}&dt=t&q={urllib.parse.quote(joined_text)}"
        req = urllib.request.Request(
            url, 
            headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'}
        )
        with urllib.request.urlopen(req, timeout=15) as response:
            result = json.loads(response.read().decode('utf-8'))
            translated_joined = "".join([part[0] for part in result[0] if part[0]])
    except Exception as e:
        print(f"  Batch translation failed for {target_lang}: {e}")
        return None

    # Split back by separator
    # Google Translate might alter spacing around separators, so use flexible regex
    split_pattern = re.compile(r'\s*\|\|\|\s*')
    translated_splits = split_pattern.split(translated_joined)

    # If the splits count does not match the batch count, do single fallback
    if len(translated_splits) != len(batch_texts):
        print(f"  Warning: Split count mismatch ({len(translated_splits)} vs {len(batch_texts)}). Falling back to single translations...")
        return None

    # Restore placeholders and clean escapes for each string
    final_translations = []
    for idx, trans in enumerate(translated_splits):
        placeholders = placeholders_map[idx]
        escapes = escapes_map[idx]

        # Restore escapes
        for i, esc in enumerate(escapes):
            trans = re.sub(rf'\s*\[\s*E{i}\s*\]\s*', esc, trans, flags=re.IGNORECASE)
            trans = trans.replace(f"[E{i}]", esc).replace(f"[e{i}]", esc)
            
        # Restore format specifiers
        for i, ph in enumerate(placeholders):
            trans = re.sub(rf'\s*\[\s*{i}\s*\]\s*', ph, trans)
            trans = trans.replace(f"[{i}]", ph)

        trans = trans.replace("&amp; ", "&amp;").replace(" &amp;", "&amp;")
        trans = clean_escapes(trans)
        final_translations.append(trans)

    return final_translations

def load_strings_xml(filepath):
    if not os.path.exists(filepath):
        return {}
    try:
        tree = ET.parse(filepath)
        root = tree.getroot()
        strings = {}
        for child in root:
            if child.tag == 'string':
                name = child.attrib.get('name')
                text = child.text or ""
                strings[name] = text
        return strings
    except Exception as e:
        print(f"Error loading {filepath}: {e}")
        return {}

def write_strings_xml(filepath, strings, master_keys_ordered):
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    lines = ['<resources>']
    for key in master_keys_ordered:
        val = strings.get(key)
        if val is not None:
            val_escaped = val.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
            val_escaped = val_escaped.replace('&amp;amp;', '&amp;')
            lines.append(f'    <string name="{key}">{val_escaped}</string>')
    lines.append('</resources>')
    
    try:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write('\n'.join(lines) + '\n')
        print(f"  Successfully wrote {len(strings)} strings to {filepath}")
    except Exception as e:
        print(f"  Error writing to {filepath}: {e}")

def translate_single_fallback(text, target_lang):
    """Fallback single text translation"""
    if not text or text.strip() == "":
        return text
    
    placeholders = []
    pattern = re.compile(r'(%[0-9]+\$[a-zA-Z]|%[a-zA-Z])')
    def repl(match):
        placeholder = match.group(0)
        index = len(placeholders)
        placeholders.append(placeholder)
        return f" [{index}] "
    temp_text = pattern.sub(repl, text)
    
    escapes = []
    def repl_escape(match):
        escape = match.group(0)
        index = len(escapes)
        escapes.append(escape)
        return f" [E{index}] "
    temp_text = re.sub(r'(\\\'|\\\"|\\n)', repl_escape, temp_text)

    try:
        url = f"https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl={target_lang}&dt=t&q={urllib.parse.quote(temp_text)}"
        req = urllib.request.Request(
            url, 
            headers={'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'}
        )
        with urllib.request.urlopen(req, timeout=10) as response:
            result = json.loads(response.read().decode('utf-8'))
            translated = "".join([part[0] for part in result[0] if part[0]])
    except Exception as e:
        return None

    # Restore
    for i, esc in enumerate(escapes):
        translated = re.sub(rf'\s*\[\s*E{i}\s*\]\s*', esc, translated, flags=re.IGNORECASE)
        translated = translated.replace(f"[E{i}]", esc).replace(f"[e{i}]", esc)
        
    for i, ph in enumerate(placeholders):
        translated = re.sub(rf'\s*\[\s*{i}\s*\]\s*', ph, translated)
        translated = translated.replace(f"[{i}]", ph)

    translated = translated.replace("&amp; ", "&amp;").replace(" &amp;", "&amp;")
    return clean_escapes(translated)

def main():
    print("Starting Android string resource translation script (BATCH MODE)...")
    if not os.path.exists(MASTER_STRINGS_PATH):
        print(f"Master strings.xml not found at {MASTER_STRINGS_PATH}!")
        return
    
    master_tree = ET.parse(MASTER_STRINGS_PATH)
    master_root = master_tree.getroot()
    
    master_strings = {}
    master_keys_ordered = []
    for child in master_root:
        if child.tag == 'string':
            name = child.attrib.get('name')
            text = child.text or ""
            master_strings[name] = text
            master_keys_ordered.append(name)
            
    print(f"Loaded {len(master_keys_ordered)} master strings.")
    
    for g_lang, folder_suffix in LANGUAGES.items():
        lang_folder = f"values-{folder_suffix}"
        target_path = os.path.join(RES_DIR, lang_folder, 'strings.xml')
        print(f"\nProcessing language '{g_lang}' ({lang_folder})...")
        
        target_strings = load_strings_xml(target_path)
        missing_keys = [k for k in master_keys_ordered if k not in target_strings or not target_strings[k]]
        
        if not missing_keys:
            print(f"  All strings are already translated for {g_lang}.")
            write_strings_xml(target_path, target_strings, master_keys_ordered)
            continue
            
        print(f"  Found {len(missing_keys)} missing strings. Batch translating...")
        
        # Batch size of 20 strings to keep URL lengths safe
        batch_size = 25
        batches = [missing_keys[i:i + batch_size] for i in range(0, len(missing_keys), batch_size)]
        
        for batch_idx, batch in enumerate(batches):
            print(f"    Translating batch {batch_idx + 1}/{len(batches)} (size {len(batch)})...")
            batch_texts = [master_strings[k] for k in batch]
            
            translated_batch = translate_batch(batch_texts, g_lang)
            
            if translated_batch:
                for idx, key in enumerate(batch):
                    target_strings[key] = translated_batch[idx]
            else:
                # Fallback to single translations for this batch
                print(f"    Batch translation failed. Running single fallback for this batch...")
                for key in batch:
                    trans = translate_single_fallback(master_strings[key], g_lang)
                    target_strings[key] = trans if trans is not None else master_strings[key]
                    time.sleep(0.1)
            
            time.sleep(0.5) # Slight throttle between batches
                
        write_strings_xml(target_path, target_strings, master_keys_ordered)
    print("\nTranslation complete!")

if __name__ == '__main__':
    main()
