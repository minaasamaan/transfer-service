package com.mybank.transferservice.resource;

import com.mybank.transferservice.dto.TransferRequest;
import com.mybank.transferservice.dto.TransferResponse;
import com.mybank.transferservice.service.TransferService;
import com.mybank.transferservice.vo.TransferVo;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Api
@Path("/accounts/{accountId}")
@Produces(MediaType.APPLICATION_JSON)
@AllArgsConstructor
@NoArgsConstructor
public class TransferResource {

    private TransferService transferService;

    @POST
    @Path("/transfers")
    public TransferResponse transfer(@PathParam("accountId") UUID fromAccountId, @NotNull @Valid TransferRequest transferRequest) {
        TransferVo transferVo = transferService.transfer(fromAccountId, transferRequest.getBeneficiaryId(), transferRequest.getAmount());
        return TransferResponse.builder()
                .transactionId(transferVo.getDebitTransactionId())
                .beneficiaryTransactionId(transferVo.getCreditTransactionId())
                .correlationId(transferVo.getCorrelationId())
                .build();
    }
}
