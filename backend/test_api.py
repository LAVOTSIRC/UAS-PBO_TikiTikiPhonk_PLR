import urllib.request
import json
import uuid

# Create a random user
username = "testuser_" + str(uuid.uuid4())[:8]
password = "password123"

# Register
req = urllib.request.Request("http://localhost:8080/api/auth/register", 
    data=json.dumps({"username": username, "email": username+"@test.com", "password": password}).encode('utf-8'),
    headers={"Content-Type": "application/json"}, method="POST")
try:
    resp = urllib.request.urlopen(req)
    print("Registered successfully")
except urllib.error.HTTPError as e:
    print("Register failed:", e.read().decode())
    exit(1)

# Login
req = urllib.request.Request("http://localhost:8080/api/auth/login", 
    data=json.dumps({"username": username, "password": password}).encode('utf-8'),
    headers={"Content-Type": "application/json"}, method="POST")
try:
    resp = urllib.request.urlopen(req)
    res_data = json.loads(resp.read().decode())
    token = res_data.get("token")
    print("Logged in, token:", token[:10] + "...")
except urllib.error.HTTPError as e:
    print("Login failed:", e.read().decode())
    exit(1)

# Change Password (correct)
req = urllib.request.Request("http://localhost:8080/api/users/change-password", 
    data=json.dumps({"oldPassword": password, "newPassword": "newpassword123"}).encode('utf-8'),
    headers={"Content-Type": "application/json", "Authorization": "Bearer " + token}, method="PUT")
try:
    resp = urllib.request.urlopen(req)
    print("Change password success:", resp.read().decode())
except urllib.error.HTTPError as e:
    print("Change password failed (Correct old pwd):", e.code, e.reason)
    print("Response body:", e.read().decode())

# Change Password (wrong)
req = urllib.request.Request("http://localhost:8080/api/users/change-password", 
    data=json.dumps({"oldPassword": "wrongpassword", "newPassword": "newpassword123"}).encode('utf-8'),
    headers={"Content-Type": "application/json", "Authorization": "Bearer " + token}, method="PUT")
try:
    resp = urllib.request.urlopen(req)
    print("Change password success:", resp.read().decode())
except urllib.error.HTTPError as e:
    print("Change password failed (Wrong old pwd):", e.code, e.reason)
    print("Response body:", e.read().decode())

