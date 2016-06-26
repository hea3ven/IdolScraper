package com.hea3ven.idolscraper.imageenhancer

class DefaultEnhancer : ImageEnhancer {
    override fun enhance(url: String): String {
        return url
    }
}
