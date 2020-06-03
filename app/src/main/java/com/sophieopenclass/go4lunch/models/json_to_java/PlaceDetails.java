
package com.sophieopenclass.go4lunch.models.json_to_java;

import android.location.Location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.sophieopenclass.go4lunch.BuildConfig;
import com.sophieopenclass.go4lunch.base.BaseActivity;

import java.util.List;

import static com.sophieopenclass.go4lunch.api.PlaceService.API_URL;
import static com.sophieopenclass.go4lunch.api.PlaceService.PHOTO_URL;

public class PlaceDetails {

    @SerializedName("address_components")
    @Expose
    private List<AddressComponent> addressComponents = null;
    @SerializedName("adr_address")
    @Expose
    private String adrAddress;
    @SerializedName("business_status")
    @Expose
    private String businessStatus;
    @SerializedName("formatted_address")
    @Expose
    private String formattedAddress;
    @SerializedName("formatted_phone_number")
    @Expose
    private String formattedPhoneNumber;
    @SerializedName("geometry")
    @Expose
    private Geometry geometry;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("international_phone_number")
    @Expose
    private String internationalPhoneNumber;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("opening_hours")
    @Expose
    private OpeningHours openingHours;
    @SerializedName("photos")
    @Expose
    private List<Photo> photos = null;
    @SerializedName("place_id")
    @Expose
    private String placeId;
    @SerializedName("plus_code")
    @Expose
    private PlusCode plusCode;
    @SerializedName("rating")
    @Expose
    private Double rating;
    @SerializedName("reference")
    @Expose
    private String reference;
    @SerializedName("reviews")
    @Expose
    private List<Review> reviews = null;
    @SerializedName("scope")
    @Expose
    private String scope;
    @SerializedName("types")
    @Expose
    private List<String> types = null;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("user_ratings_total")
    @Expose
    private Integer userRatingsTotal;
    @SerializedName("utc_offset")
    @Expose
    private Integer utcOffset;
    @SerializedName("vicinity")
    @Expose
    private String vicinity;
    @SerializedName("website")
    @Expose
    private String website;
    private int nbrOfWorkmates;


    // --- GETTERS ---

    public Geometry getGeometry() {
        return geometry;
    }
    public String getIcon() {
        return icon;
    }
    public String getId() {
        return id;
    }
    public String getInternationalPhoneNumber() {
        return internationalPhoneNumber;
    }
    public String getName() {
        return name;
    }
    public OpeningHours getOpeningHours() {
        return openingHours;
    }
    public List<Photo> getPhotos() {
        return photos;
    }
    public String getPlaceId() {
        return placeId;
    }
    public Double getRating() {
        return rating;
    }
    public String getVicinity() {
        return vicinity;
    }
    public String getWebsite() {
        return website;
    }
    public int getNbrOfWorkmates() {
        return nbrOfWorkmates;
    }

    public int getDistance() {
        Location restaurantLocation = new Location(this.getName());
        restaurantLocation.setLatitude(this.getGeometry().getLocation().getLat());
        restaurantLocation.setLongitude(this.getGeometry().getLocation().getLng());
        return (int) restaurantLocation.distanceTo(BaseActivity.sCurrentLocation);
    }

    public PlusCode getPlusCode() {
        return plusCode;
    }
    public String getFormattedAddress() {
        return formattedAddress;
    }
    public String getFormattedPhoneNumber() {
        return formattedPhoneNumber;
    }
    public List<AddressComponent> getAddressComponents() {
        return addressComponents;
    }
    public String getAdrAddress() {
        return adrAddress;
    }
    public String getBusinessStatus() {
        return businessStatus;
    }
    public String getReference() {
        return reference;
    }
    public List<Review> getReviews() {
        return reviews;
    }
    public String getScope() {
        return scope;
    }
    public List<String> getTypes() {
        return types;
    }
    public String getUrl() {
        return url;
    }
    public Integer getUserRatingsTotal() {
        return userRatingsTotal;
    }
    public Integer getUtcOffset() {
        return utcOffset;
    }

    // --- SETTERS ---
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
    public void setNbrOfWorkmates(int nbrOfWorkmates) {
        this.nbrOfWorkmates = nbrOfWorkmates;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }
    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
    public void setFormattedPhoneNumber(String formattedPhoneNumber) {
        this.formattedPhoneNumber = formattedPhoneNumber;
    }
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    public void setInternationalPhoneNumber(String internationalPhoneNumber) {
        this.internationalPhoneNumber = internationalPhoneNumber;
    }
    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }
    public void setPlusCode(PlusCode plusCode) {
        this.plusCode = plusCode;
    }
    public void setRating(Double rating) {
        this.rating = rating;
    }
    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
    public void setWebsite(String website) {
        this.website = website;
    }
    public void setAddressComponents(List<AddressComponent> addressComponents) {
        this.addressComponents = addressComponents;
    }
    public void setAdrAddress(String adrAddress) {
        this.adrAddress = adrAddress;
    }
    public void setBusinessStatus(String businessStatus) {
        this.businessStatus = businessStatus;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }
    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }
    public void setTypes(List<String> types) {
        this.types = types;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setUserRatingsTotal(Integer userRatingsTotal) {
        this.userRatingsTotal = userRatingsTotal;
    }
    public void setUtcOffset(Integer utcOffset) {
        this.utcOffset = utcOffset;
    }

    // UTILS
    public static String urlPhotoFormatter(PlaceDetails placeDetails, int position) {
        if (placeDetails.getPhotos() != null) {
            String photoReference = placeDetails.getPhotos().get(position).getPhotoReference();
            return API_URL + PHOTO_URL + photoReference + "&key=" + BuildConfig.API_KEY;
        }
        return "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ffishtankclub.com%2Fwp-content%2Fuploads%2F2016%2F09%2FimgUnavailable.png&f=1&nofb=1";
    }
}
