package com.grad.project.nc.service.instances;

import com.grad.project.nc.model.ProductInstance;
import com.grad.project.nc.persistence.ProductInstanceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstanceServiceImpl implements InstanceService {

    private ProductInstanceDao productInstanceDao;

    @Autowired
    public InstanceServiceImpl(ProductInstanceDao productInstanceDao) {
        this.productInstanceDao = productInstanceDao;
    }

    @Override
    public ProductInstance getById(Long id) {
        return productInstanceDao.find(id);
    }
}