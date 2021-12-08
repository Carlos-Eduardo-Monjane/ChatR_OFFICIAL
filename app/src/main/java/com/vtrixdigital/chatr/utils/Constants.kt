package com.vtrixdigital.chatr.utils

class Constants {
    companion object {
        var COUNTRY_CODE  = "+91"
        var COUNTRY_CODE_WITHOUT_PLUS  = "91"
        var ENABLE_ADMOB_ADS = true

        var isPurchased = false
        const val licenseKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsBo1AyD2xQeB1b7imlrrl8EQUJ7HWdiRssZBxBXcSKVjGtan4hBFt4Q6gKH6mtbB0fFDuBzWy+YDPDjY0gE4U9QDRI48rxU8aA2bhLIBjWUgx/EIHGll+tYC9KVfIYGVEw7butamjO7jvKsCBQFwDMJnHEGlKMxB//2isqUH/EbUZpV4eNl6v/VNMbJtcMD6YBA+bbDNlGQ3PrMYfFDCseNxxkePUjKxydFzke54UVyAzO85loLUWrLWmjRlcRzR7ek50SMZmXjWMJRdQg++1yt/HngT6FAm/+hthBNuhPemLxgmF82NNnGcorbWguZHc/TQSK/vXkBcyns8ktjrIwIDAQAB"
        const val productId = "premium_upgrade1"

        const val autoReplyLimitWithoutPurchase = 20
        const val autoReplyLimitWithPurchase = 2000

        const val bulkMessagingLimitWithoutPurchase = 20
        const val bulkMessagingLimitWithPurchase = 2000

        var requestedForPremiumPopup = 0
    }

    fun showAds():Boolean{
        return if(isPurchased)
            false
        else
            ENABLE_ADMOB_ADS
    }

    fun getAutoReplyLimit():Int{
        return if(isPurchased){
            autoReplyLimitWithPurchase
        }else{
            autoReplyLimitWithoutPurchase
        }
    }

    fun getBulkMessagingLimit():Int{
        return if(isPurchased){
            bulkMessagingLimitWithPurchase
        }else{
            bulkMessagingLimitWithoutPurchase
        }
    }

    fun showPremiumPopup(): Boolean{
        if(!isPurchased) {
            requestedForPremiumPopup++
            if (requestedForPremiumPopup == 5) {
                requestedForPremiumPopup = 0
                return true
            }
        }
        return false
    }
}