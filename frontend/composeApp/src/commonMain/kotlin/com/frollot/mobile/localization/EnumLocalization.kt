package com.frollot.mobile.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.frollot.mobile.model.*

/**
 * Helpers pour la localisation des enums.
 * Remplace les méthodes getDisplayName() et getDescription() hardcodées.
 */

// ========================================
// BookingStatus
// ========================================
@Composable
fun BookingStatus.getLocalizedDisplayName(): String {
    return when (this) {
        BookingStatus.PENDING -> stringResource(Strings.Enums.BookingStatus.Pending)
        BookingStatus.CONFIRMED -> stringResource(Strings.Enums.BookingStatus.Confirmed)
        BookingStatus.IN_PROGRESS -> stringResource(Strings.Enums.BookingStatus.InProgress)
        BookingStatus.COMPLETED -> stringResource(Strings.Enums.BookingStatus.Completed)
        BookingStatus.CANCELLED -> stringResource(Strings.Enums.BookingStatus.Cancelled)
        BookingStatus.NO_SHOW -> stringResource(Strings.Enums.BookingStatus.NoShow)
    }
}

// ========================================
// PaymentStatus
// ========================================
@Composable
fun PaymentStatus.getLocalizedDisplayName(): String {
    return when (this) {
        PaymentStatus.PENDING -> stringResource(Strings.Enums.PaymentStatus.Pending)
        PaymentStatus.PROCESSING -> stringResource(Strings.Enums.PaymentStatus.Processing)
        PaymentStatus.SUCCEEDED -> stringResource(Strings.Enums.PaymentStatus.Succeeded)
        PaymentStatus.FAILED -> stringResource(Strings.Enums.PaymentStatus.Failed)
        PaymentStatus.CANCELED -> stringResource(Strings.Enums.PaymentStatus.Canceled)
        PaymentStatus.PARTIALLY_REFUNDED -> stringResource(Strings.Enums.PaymentStatus.PartiallyRefunded)
        PaymentStatus.UNPAID -> stringResource(Strings.Enums.PaymentStatus.Unpaid)
        PaymentStatus.PAID -> stringResource(Strings.Enums.PaymentStatus.Paid)
        PaymentStatus.REFUNDED -> stringResource(Strings.Enums.PaymentStatus.Refunded)
    }
}

// ========================================
// PostType
// ========================================
@Composable
fun PostType.getLocalizedDisplayName(): String {
    return when (this) {
        PostType.GENERAL -> stringResource(Strings.Enums.PostType.General)
        PostType.AVANT_APRES -> stringResource(Strings.Enums.PostType.AvantApres)
        PostType.PORTFOLIO -> stringResource(Strings.Enums.PostType.Portfolio)
        PostType.TENDANCE -> stringResource(Strings.Enums.PostType.Tendance)
        PostType.CONSEIL -> stringResource(Strings.Enums.PostType.Conseil)
        PostType.REALISATION -> stringResource(Strings.Enums.PostType.Realisation)
        PostType.INSPIRATION -> stringResource(Strings.Enums.PostType.Inspiration)
    }
}

@Composable
fun PostType.getLocalizedDescription(): String {
    return when (this) {
        PostType.GENERAL -> stringResource(Strings.Enums.PostType.GeneralDescription)
        PostType.AVANT_APRES -> stringResource(Strings.Enums.PostType.AvantApresDescription)
        PostType.PORTFOLIO -> stringResource(Strings.Enums.PostType.PortfolioDescription)
        PostType.TENDANCE -> stringResource(Strings.Enums.PostType.TendanceDescription)
        PostType.CONSEIL -> stringResource(Strings.Enums.PostType.ConseilDescription)
        PostType.REALISATION -> stringResource(Strings.Enums.PostType.RealisationDescription)
        PostType.INSPIRATION -> stringResource(Strings.Enums.PostType.InspirationDescription)
    }
}

// ========================================
// PostVisibility
// ========================================
@Composable
fun PostVisibility.getLocalizedDisplayName(): String {
    return when (this) {
        PostVisibility.PUBLIC -> stringResource(Strings.Enums.PostVisibility.Public)
        PostVisibility.FOLLOWERS -> stringResource(Strings.Enums.PostVisibility.Followers)
        PostVisibility.PRIVATE -> stringResource(Strings.Enums.PostVisibility.Private)
    }
}

@Composable
fun PostVisibility.getLocalizedDescription(): String {
    return when (this) {
        PostVisibility.PUBLIC -> stringResource(Strings.Enums.PostVisibility.PublicDescription)
        PostVisibility.FOLLOWERS -> stringResource(Strings.Enums.PostVisibility.FollowersDescription)
        PostVisibility.PRIVATE -> stringResource(Strings.Enums.PostVisibility.PrivateDescription)
    }
}

// ========================================
// ServiceCategory
// ========================================
@Composable
fun ServiceCategory.getLocalizedDisplayName(): String {
    return when (this) {
        ServiceCategory.COUPE -> stringResource(Strings.Enums.ServiceCategory.Coupe)
        ServiceCategory.COLORATION -> stringResource(Strings.Enums.ServiceCategory.Coloration)
        ServiceCategory.SOIN -> stringResource(Strings.Enums.ServiceCategory.Soin)
        ServiceCategory.COIFFAGE -> stringResource(Strings.Enums.ServiceCategory.Coiffage)
        ServiceCategory.BARBE -> stringResource(Strings.Enums.ServiceCategory.Barbe)
        ServiceCategory.TECHNIQUE -> stringResource(Strings.Enums.ServiceCategory.Technique)
        ServiceCategory.AUTRE -> stringResource(Strings.Enums.ServiceCategory.Autre)
    }
}

// ========================================
// ReactionType
// ========================================
@Composable
fun ReactionType.getLocalizedDisplayName(): String {
    return when (this) {
        ReactionType.LIKE -> stringResource(Strings.Enums.ReactionType.Like)
        ReactionType.LOVE -> stringResource(Strings.Enums.ReactionType.Love)
        ReactionType.WOW -> stringResource(Strings.Enums.ReactionType.Wow)
        ReactionType.INSPIRANT -> stringResource(Strings.Enums.ReactionType.Inspirant)
        ReactionType.MAGNIFIQUE -> stringResource(Strings.Enums.ReactionType.Magnifique)
        ReactionType.BRAVO -> stringResource(Strings.Enums.ReactionType.Bravo)
    }
}

@Composable
fun ReactionType.getLocalizedDescription(): String {
    return when (this) {
        ReactionType.LIKE -> stringResource(Strings.Enums.ReactionType.LikeDescription)
        ReactionType.LOVE -> stringResource(Strings.Enums.ReactionType.LoveDescription)
        ReactionType.WOW -> stringResource(Strings.Enums.ReactionType.WowDescription)
        ReactionType.INSPIRANT -> stringResource(Strings.Enums.ReactionType.InspirantDescription)
        ReactionType.MAGNIFIQUE -> stringResource(Strings.Enums.ReactionType.MagnifiqueDescription)
        ReactionType.BRAVO -> stringResource(Strings.Enums.ReactionType.BravoDescription)
    }
}

// ========================================
// PostMediaType
// ========================================
@Composable
fun PostMediaType.getLocalizedDisplayName(): String {
    return when (this) {
        PostMediaType.before -> stringResource(Strings.Enums.MediaType.Before)
        PostMediaType.after -> stringResource(Strings.Enums.MediaType.After)
        PostMediaType.process -> stringResource(Strings.Enums.MediaType.Process)
        PostMediaType.detail -> stringResource(Strings.Enums.MediaType.Detail)
    }
}

// ========================================
// ReportReason
// ========================================
@Composable
fun ReportReason.getLocalizedDisplayName(): String {
    return when (this) {
        ReportReason.INAPPROPRIE -> stringResource(Strings.Enums.ReportReason.Inapproprie)
        ReportReason.SPAM -> stringResource(Strings.Enums.ReportReason.Spam)
        ReportReason.FAUX -> stringResource(Strings.Enums.ReportReason.Faux)
        ReportReason.COPYRIGHT -> stringResource(Strings.Enums.ReportReason.Copyright)
        ReportReason.AUTRE -> stringResource(Strings.Enums.ReportReason.Autre)
    }
}

@Composable
fun ReportReason.getLocalizedDescription(): String {
    return when (this) {
        ReportReason.INAPPROPRIE -> stringResource(Strings.Enums.ReportReason.InapproprieDescription)
        ReportReason.SPAM -> stringResource(Strings.Enums.ReportReason.SpamDescription)
        ReportReason.FAUX -> stringResource(Strings.Enums.ReportReason.FauxDescription)
        ReportReason.COPYRIGHT -> stringResource(Strings.Enums.ReportReason.CopyrightDescription)
        ReportReason.AUTRE -> stringResource(Strings.Enums.ReportReason.AutreDescription)
    }
}

// ========================================
// ReportedEntityType
// ========================================
@Composable
fun ReportedEntityType.getLocalizedDisplayName(): String {
    return when (this) {
        ReportedEntityType.POST -> stringResource(Strings.Enums.ReportedEntityType.Post)
        ReportedEntityType.COMMENT -> stringResource(Strings.Enums.ReportedEntityType.Comment)
        ReportedEntityType.USER -> stringResource(Strings.Enums.ReportedEntityType.User)
        ReportedEntityType.SALON -> stringResource(Strings.Enums.ReportedEntityType.Salon)
    }
}

// ========================================
// VerificationType
// ========================================
@Composable
fun VerificationType.getLocalizedDisplayName(): String {
    return when (this) {
        VerificationType.EMAIL -> stringResource(Strings.Enums.VerificationType.Email)
        VerificationType.PHONE -> stringResource(Strings.Enums.VerificationType.Phone)
        VerificationType.BUSINESS -> stringResource(Strings.Enums.VerificationType.Business)
        VerificationType.PROFESSIONAL -> stringResource(Strings.Enums.VerificationType.Professional)
    }
}

@Composable
fun VerificationType.getLocalizedDescription(): String {
    return when (this) {
        VerificationType.EMAIL -> stringResource(Strings.Enums.VerificationType.EmailDescription)
        VerificationType.PHONE -> stringResource(Strings.Enums.VerificationType.PhoneDescription)
        VerificationType.BUSINESS -> stringResource(Strings.Enums.VerificationType.BusinessDescription)
        VerificationType.PROFESSIONAL -> stringResource(Strings.Enums.VerificationType.ProfessionalDescription)
    }
}

// ========================================
// BadgeCategory
// ========================================
@Composable
fun BadgeCategory.getLocalizedDisplayName(): String {
    return when (this) {
        BadgeCategory.CERTIFICATION -> stringResource(Strings.Enums.BadgeCategory.Certification)
        BadgeCategory.COMPETITION -> stringResource(Strings.Enums.BadgeCategory.Competition)
        BadgeCategory.FORMATION -> stringResource(Strings.Enums.BadgeCategory.Formation)
        BadgeCategory.PARTENARIAT -> stringResource(Strings.Enums.BadgeCategory.Partenariat)
    }
}

// ========================================
// ReportStatus
// ========================================
@Composable
fun ReportStatus.getLocalizedDisplayName(): String {
    return when (this) {
        ReportStatus.PENDING -> stringResource(Strings.Enums.ReportStatus.Pending)
        ReportStatus.REVIEWED -> stringResource(Strings.Enums.ReportStatus.Reviewed)
        ReportStatus.RESOLVED -> stringResource(Strings.Enums.ReportStatus.Resolved)
        ReportStatus.DISMISSED -> stringResource(Strings.Enums.ReportStatus.Dismissed)
    }
}

// ========================================
// ModerationActionType
// ========================================
@Composable
fun ModerationActionType.getLocalizedDisplayName(): String {
    return when (this) {
        ModerationActionType.HIDE -> stringResource(Strings.Enums.ModerationAction.Hide)
        ModerationActionType.DELETE -> stringResource(Strings.Enums.ModerationAction.Delete)
        ModerationActionType.WARN -> stringResource(Strings.Enums.ModerationAction.Warn)
    }
}

@Composable
fun ModerationActionType.getLocalizedDescription(): String {
    return when (this) {
        ModerationActionType.HIDE -> stringResource(Strings.Enums.ModerationAction.HideDescription)
        ModerationActionType.DELETE -> stringResource(Strings.Enums.ModerationAction.DeleteDescription)
        ModerationActionType.WARN -> stringResource(Strings.Enums.ModerationAction.WarnDescription)
    }
}

// ========================================
// AppealStatus
// ========================================
@Composable
fun AppealStatus.getLocalizedDisplayName(): String {
    return when (this) {
        AppealStatus.NONE -> stringResource(Strings.Enums.AppealStatus.None)
        AppealStatus.PENDING -> stringResource(Strings.Enums.AppealStatus.Pending)
        AppealStatus.APPROVED -> stringResource(Strings.Enums.AppealStatus.Approved)
        AppealStatus.REJECTED -> stringResource(Strings.Enums.AppealStatus.Rejected)
    }
}

// ========================================
// HairHashtagCategory
// ========================================
@Composable
fun HairHashtagCategory.getLocalizedDisplayName(): String {
    return when (this) {
        HairHashtagCategory.TECHNIQUE -> stringResource(Strings.Enums.HairHashtagCategory.Technique)
        HairHashtagCategory.STYLE -> stringResource(Strings.Enums.HairHashtagCategory.Style)
        HairHashtagCategory.COULEUR -> stringResource(Strings.Enums.HairHashtagCategory.Couleur)
        HairHashtagCategory.LONGUEUR -> stringResource(Strings.Enums.HairHashtagCategory.Longueur)
        HairHashtagCategory.TEXTURE -> stringResource(Strings.Enums.HairHashtagCategory.Texture)
    }
}

// ========================================
// FollowingType
// ========================================
@Composable
fun FollowingType.getLocalizedDisplayName(): String {
    return when (this) {
        FollowingType.USER -> "Utilisateur" // Pas de clé spécifique, utiliser directement
        FollowingType.SALON -> stringResource(Strings.Enums.FollowingType.Salon)
        FollowingType.COIFFEUR -> stringResource(Strings.Enums.FollowingType.Coiffeur)
    }
}

