rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isAccessingOwnData(userId) {
      return request.auth.uid == userId;
    }
    
    function isAdmin() {
      return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true;
    }
    
    // Default deny all access
    match /{document=**} {
      allow read, write: if false;
    }
    
    // User data - users can write their own data
    match /users/{userId} {
      // Allow authenticated users to create their own user document
      allow create, update: if isAuthenticated() && isAccessingOwnData(userId);
      
      // Allow authenticated users to read their own user document
      allow read: if isAuthenticated() && (isAccessingOwnData(userId) || isAdmin());
    }
    
    // Crops collection - all authenticated users can read
    match /crops/{cropId} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && isAdmin();
    }
    
    // Identification results - users can only access their own results
    match /identificationResults/{resultId} {
      allow read, create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
      allow update, delete: if isAuthenticated() && resource.data.userId == request.auth.uid;
    }
    
    // Usage limits - users can read their own usage data but not modify it
    match /usageLimits/{userId} {
      allow read: if isAuthenticated() && isAccessingOwnData(userId);
      allow write: if isAuthenticated() && isAdmin();
    }
    
    // Admin can read and write all data
    match /{path=**} {
      allow read, write: if isAuthenticated() && isAdmin();
    }
  }
}