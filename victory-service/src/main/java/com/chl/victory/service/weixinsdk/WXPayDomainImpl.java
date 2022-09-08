package com.chl.victory.service.weixinsdk;

/**
 * @author ChenHailong
 * @date 2020/2/19 12:34
 **/
public class WXPayDomainImpl implements IWXPayDomain {

    @Override
    public void report(String domain, long elapsedTimeMillis, Exception ex) {

    }

    @Override
    public DomainInfo getDomain(WXPayConfig config) {
        DomainInfo domainInfo = new DomainInfo(WXPayConstants.DOMAIN_API, true);
        return domainInfo;
    }
}
