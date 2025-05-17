rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      // Allow read/write access if authenticated
      allow read, write: if request.auth != null;
      
      // For profile images and uploads
      match /profileImages/{userId} {
        // Allow users to read all profile images
        allow read: if true;
        
        // Allow users to upload their own profile images when authenticated
        allow write: if request.auth != null && request.auth.uid == userId && 
                      request.resource.size < 5 * 1024 * 1024; // 5MB max
      }
      
      // For crop identification images
      match /cropImages/{imageId} {
        // Allow authenticated users to upload images
        allow write: if request.auth != null && 
                      request.resource.size < 10 * 1024 * 1024; // 10MB max
        
        // Only allow users to read their own uploads
        allow read: if request.auth != null;
      }
    }
  }
}

