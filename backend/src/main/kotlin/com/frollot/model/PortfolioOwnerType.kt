package com.frollot.model

/**
 * Énumération des types de propriétaires de portfolios.
 */
enum class PortfolioOwnerType {
    /**
     * Portfolio appartenant à un coiffeur (User de type hairstylist).
     */
    coiffeur,

    /**
     * Portfolio appartenant à un salon.
     */
    salon;

    /**
     * Retourne le libellé utilisateur du type.
     */
    fun getDisplayName(): String {
        return when (this) {
            coiffeur -> "Coiffeur"
            salon -> "Salon"
        }
    }
}

