import os

def fix_profile_screens(path):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Fix MiniBabyAvatar calls
    content = content.replace('faceOnly = true,', 'completedPercentage = 0f,')
    
    # Fix imports
    imports = content.split('\n')
    new_imports = []
    seen = set()
    for line in imports:
        if line.startswith('import '):
            if line not in seen:
                new_imports.append(line)
                seen.add(line)
        else:
            break
            
    # Ensure essential imports are present
    essential = [
        'import androidx.compose.runtime.*',
        'import androidx.lifecycle.compose.collectAsStateWithLifecycle',
        'import androidx.compose.material.icons.Icons',
        'import androidx.compose.material.icons.filled.*',
        'import androidx.compose.foundation.BorderStroke'
    ]
    for imp in essential:
        if imp not in seen:
            new_imports.append(imp)
            
    # Rebuild content
    # Find where imports end
    first_non_import = 0
    for i, line in enumerate(imports):
        if not line.startswith('import ') and not line.startswith('package ') and line.strip():
            first_non_import = i
            break
            
    # Combine package (if exists), new imports, and the rest of the file
    package_line = imports[0] if imports[0].startswith('package ') else ""
    rest_of_file = '\n'.join(imports[1 if package_line else 0:])
    # Remove old imports from the rest of the file
    # This is tricky. Let's just replace the whole import block.
    
    # Simple approach: replace all import lines with our new set
    lines = content.split('\n')
    final_lines = []
    import_block = []
    package_line = ""
    
    for line in lines:
        if line.startswith('package '):
            package_line = line
        elif line.startswith('import '):
            import_block.append(line)
        elif not line.strip():
            if not import_block:
                final_lines.append(line)
            elif import_block:
                # We reached the end of imports
                # Add the curated ones instead
                curated = []
                seen_curated = set()
                # Keep order of existing imports but filter/add
                for imp in import_block:
                    if imp not in seen_curated:
                        curated.append(imp)
                        seen_curated.add(imp)
                
                for ess in essential:
                    if ess not in seen_curated:
                        curated.append(ess)
                
                final_lines.extend(curated)
                import_block = []
                final_lines.append(line)
        else:
            if import_block:
                curated = []
                seen_curated = set()
                for imp in import_block:
                    if imp not in seen_curated:
                        curated.append(imp)
                        seen_curated.add(imp)
                for ess in essential:
                    if ess not in seen_curated:
                        curated.append(ess)
                final_lines.extend(curated)
                import_block = []
            final_lines.append(line)
            
    # Put package at top
    if package_line:
        final_lines.insert(0, package_line)
        
    # Write back
    with open(path, 'w', encoding='utf-8') as f:
        f.write('\n'.join(final_lines))

def fix_moments_screens(path):
    with open(path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    # Fix the trailing braces
    # Find the last non-empty line
    last_line = len(lines) - 1
    while last_line >= 0 and not lines[last_line].strip():
        last_line -= 1
    
    # Keep only the necessary closing braces for the main function
    # Based on the file, we need 2: one for the Column/Box and one for the function
    # But wait, it's better to just count them.
    
    # For now, let's just ensure it ends with exactly two '}' if it's a single function
    # Looking at the file, it has MomentsScreen function.
    
    # Let's just trim the end to remove the "Expecting a top level declaration" error
    # which usually means an extra } at the end.
    
    # Remove the very last brace if there are too many
    # A safer way: remove any line that is just '}' and follows a '}' that already closed the function.
    
    # Let's just remove the line 567 and 568 as reported by Gradle
    # In Python, this is index 566 and 567
    if len(lines) > 567:
        # We'll just keep everything up to the last few and then manually add the closing ones
        # But the file has many nested blocks.
        pass

    # Actually, let's just use a simple rule: if the file ends with more than 2 closing braces, 
    # it might be wrong. But it's safer to just remove the offending line.
    # The error was at 567:1. Let's just remove the last line.
    if lines:
        lines.pop() 
        
    with open(path, 'w', encoding='utf-8') as f:
        f.writelines(lines)

fix_profile_screens('app/src/main/java/com/example/ui/ProfileScreens.kt')
fix_moments_screens('app/src/main/java/com/example/ui/MomentsScreens.kt')
