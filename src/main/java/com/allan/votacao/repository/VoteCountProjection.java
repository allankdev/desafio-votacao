package com.allan.votacao.repository;

import com.allan.votacao.model.VoteOption;

public interface VoteCountProjection {

    VoteOption getChoice();

    long getTotal();
}
