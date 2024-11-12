package pl.pbgym.dto.user.trainer;

import java.util.List;

public class GetPublicTrainerInfoWithOffersResponseDto {
    private GetPublicTrainerInfoResponseDto trainerInfo;

    private List<GetTrainerOfferResponseDto> trainerOffers;

    public GetPublicTrainerInfoResponseDto getTrainerInfo() {
        return trainerInfo;
    }

    public void setTrainerInfo(GetPublicTrainerInfoResponseDto trainerInfo) {
        this.trainerInfo = trainerInfo;
    }

    public List<GetTrainerOfferResponseDto> getTrainerOffers() {
        return trainerOffers;
    }

    public void setTrainerOffers(List<GetTrainerOfferResponseDto> trainerOffers) {
        this.trainerOffers = trainerOffers;
    }
}
