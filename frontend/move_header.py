import re

filepath = "src/main/resources/fxml/ProfilePanel.fxml"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Extract header card
header_match = re.search(r'(\s*<!-- ========== HEADER CARD ========== -->\s*<VBox styleClass="profile-card">.*?)</VBox>\s*<!-- ========== SCROLLABLE SETTINGS ========== -->', content, re.DOTALL)

if not header_match:
    print("Header not found")
    exit(1)

header_content = header_match.group(1) + "</VBox>\n"

# Remove the VBox.margin from the header content
header_content = re.sub(r'\s*<VBox\.margin>.*?</VBox\.margin>', '', header_content, flags=re.DOTALL)

# Remove the header from the original content
content = content.replace(header_match.group(0), "    <!-- ========== SCROLLABLE SETTINGS ========== -->")

# Find the insertion point inside the scrollable settings
scroll_content_match = re.search(r'(<VBox styleClass="profile-scroll-content" spacing="20">\s*<VBox\.margin>.*?</VBox\.margin>\s*)', content, re.DOTALL)

if not scroll_content_match:
    print("Scroll content not found")
    exit(1)

# Keep VBox.margin because it was there, but insert the header right after it
insertion = scroll_content_match.group(1) + "\n" + header_content

content = content.replace(scroll_content_match.group(1), insertion)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)

print("Successfully moved the header inside the ScrollPane.")
