package webcam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import webcam.config.CelebrityProperties;
import webcam.service.impl.CelebrityPhotoServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

class CelebrityPhotoServiceTest {

    private CelebrityPhotoService celebrityPhotoService;
    private CelebrityProperties celebrityProperties;

    @BeforeEach
    void setUp() {
        celebrityProperties = new CelebrityProperties();
        celebrityPhotoService = new CelebrityPhotoServiceImpl(celebrityProperties);
    }

    @Test
    void testGetRandomMaleCelebrityPhoto() {
        String photoUrl = celebrityPhotoService.getRandomCelebrityPhoto("male");
        assertNotNull(photoUrl, "Male celebrity photo URL should not be null");
        assertTrue(photoUrl.startsWith("http"), "Photo URL should be a valid URL");
    }

    @Test
    void testGetRandomFemaleCelebrityPhoto() {
        String photoUrl = celebrityPhotoService.getRandomCelebrityPhoto("female");
        assertNotNull(photoUrl, "Female celebrity photo URL should not be null");
        assertTrue(photoUrl.startsWith("http"), "Photo URL should be a valid URL");
    }

    @Test
    void testGetRandomCelebrityPhoto_CaseInsensitive() {
        String malePhoto = celebrityPhotoService.getRandomCelebrityPhoto("MALE");
        assertNotNull(malePhoto, "Male celebrity photo URL should not be null");

        String femalePhoto = celebrityPhotoService.getRandomCelebrityPhoto("FEMALE");
        assertNotNull(femalePhoto, "Female celebrity photo URL should not be null");
    }

    @Test
    void testGetRandomCelebrityPhoto_ReturnsRandomPhotos() {
        // Call the method multiple times and verify we get valid URLs
        for (int i = 0; i < 10; i++) {
            String malePhoto = celebrityPhotoService.getRandomCelebrityPhoto("male");
            assertNotNull(malePhoto);
            assertTrue(celebrityProperties.getMalePhotos().contains(malePhoto),
                    "Male photo should be from the configured list");

            String femalePhoto = celebrityPhotoService.getRandomCelebrityPhoto("female");
            assertNotNull(femalePhoto);
            assertTrue(celebrityProperties.getFemalePhotos().contains(femalePhoto),
                    "Female photo should be from the configured list");
        }
    }

    @Test
    void testCelebrityProperties_HasPhotos() {
        assertFalse(celebrityProperties.getMalePhotos().isEmpty(),
                "Male photos list should not be empty");
        assertFalse(celebrityProperties.getFemalePhotos().isEmpty(),
                "Female photos list should not be empty");

        assertEquals(10, celebrityProperties.getMalePhotos().size(),
                "Should have 10 male celebrity photos");
        assertEquals(10, celebrityProperties.getFemalePhotos().size(),
                "Should have 10 female celebrity photos");
    }
}
