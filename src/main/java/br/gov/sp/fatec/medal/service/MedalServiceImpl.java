package br.gov.sp.fatec.medal.service;

import br.gov.sp.fatec.medal.domain.Medal;
import br.gov.sp.fatec.medal.repository.MedalRepository;
import br.gov.sp.fatec.medal.service.MedalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class MedalServiceImpl implements MedalService {

    @Autowired
    private MedalRepository repository;

    @PreAuthorize("isAuthenticated()")
    public List<Medal> findAll() {
        return repository.findAll();
    }
}
