import React, { useMemo, useState } from 'react';
import { View, Text, TouchableOpacity, Pressable, Modal, StyleSheet } from 'react-native';
import { Image } from 'expo-image';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';
import { router } from 'expo-router';
import { Avatar } from '../common/Avatar';
import { PostResponse, TaggedType } from '../../types';
import { useTheme } from '../../theme';
import { resolveMediaUrl } from '../../utils/media';
import { formatRelativeShort } from '../../utils/formatDate';

/** Is this post published "as salon" (mode 3)? */
const isSalonPost = (post: PostResponse) => post.postAuthorType === 'salon' && post.salonName;

interface PostCardProps {
  post: PostResponse;
  currentUserId?: string;
  onLike?: () => void;
  onComment?: () => void;
  onShare?: () => void;
  onBookmark?: () => void;
  onPress?: () => void;
  onProfilePress?: () => void;
  onDelete?: () => void;
  onReport?: () => void;
  /** Affiché dans le menu uniquement si fourni (écran Archives) */
  onUnarchive?: () => void;
  /** Affiché dans le menu uniquement si fourni — classement en collection (B30) */
  onSaveToCollection?: () => void;
  /** Affiché dans le menu uniquement si fourni ET post possédé (B32) */
  onArchive?: () => void;
  /** Affiché dans le menu uniquement si fourni ET post possédé (B29) — bascule selon post.isPinned */
  onPin?: () => void;
}

export function PostCard({
  post, currentUserId, onLike, onComment, onShare,
  onBookmark, onPress, onProfilePress, onDelete, onReport, onUnarchive, onSaveToCollection, onArchive, onPin,
}: PostCardProps) {
  const { colors } = useTheme();
  const { t } = useTranslation();
  const [showMenu, setShowMenu] = useState(false);
  const hashtags = post.content?.match(/#\w+/g) || [];
  const textWithoutTags = post.content?.replace(/#\w+/g, '').trim();
  const isOwn = currentUserId && post.authorId === currentUserId;
  const salonTags = useMemo(
    () => (post.tags ?? []).filter(tag => tag.taggedType === TaggedType.SALON && tag.taggedName),
    [post.tags],
  );
  const relativeDate = useMemo(() => {
    if (!post.createdAt) return '';
    const d = new Date(post.createdAt);
    return formatRelativeShort(d, t);
  }, [post.createdAt, t]);

  return (
    <View style={[styles.article, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={isSalonPost(post) ? () => router.push(`/salon/${post.salonId}`) : onProfilePress}>
          <Avatar
            initials={isSalonPost(post) ? (post.salonName?.[0] || 'S') : (post.authorName?.[0] || 'U')}
            size={44}
            ring
            tone={isSalonPost(post) ? 'tertiary' : 'primary'}
            imageUrl={isSalonPost(post) ? (post.salonAvatarUrl ? resolveMediaUrl(post.salonAvatarUrl) : undefined) : post.authorAvatarUrl}
          />
        </TouchableOpacity>
        <View style={styles.headerInfo}>
          <View style={styles.nameRow}>
            <Text style={[styles.name, { color: colors.onSurface }]} numberOfLines={1}>
              {isSalonPost(post) ? post.salonName : post.authorName}
            </Text>
            <MaterialCommunityIcons name="check-decagram" size={16} color={colors.primary} />
          </View>
          <View style={styles.metaRow}>
            {isSalonPost(post) ? (
              <View style={[styles.typeBadge, { backgroundColor: colors.tertiaryContainer }]}>
                <Text style={[styles.typeText, { color: colors.tertiary }]}>{t('social.authorTypes.salonOwner')}</Text>
              </View>
            ) : post.authorUserType ? (
              <View style={[styles.typeBadge, { backgroundColor: colors.secondaryContainer }]}>
                <Text style={[styles.typeText, { color: colors.secondary }]}>{post.authorUserType === 'salon_owner' ? t('social.authorTypes.salonOwner') : t('social.authorTypes.hairstylist')}</Text>
              </View>
            ) : null}
            {isSalonPost(post) && post.authorName ? (
              <TouchableOpacity onPress={onProfilePress}>
                <Text style={[styles.byAuthor, { color: colors.onSurfaceVariant }]} numberOfLines={1}>
                  {'\u00B7 ' + t('social.byAuthor', { name: post.authorName })}
                </Text>
              </TouchableOpacity>
            ) : null}
            {salonTags.length > 0 && !isSalonPost(post) && (
              <TouchableOpacity style={styles.salonTagBtn} onPress={() => router.push(`/salon/${salonTags[0].taggedId}`)}>
                <Text style={[styles.salonTag, { color: colors.tertiary }]} numberOfLines={1}>
                  {'\u00B7 ' + t('social.taggedSalon', { name: salonTags.map(tag => tag.taggedName).join(', ') })}
                </Text>
              </TouchableOpacity>
            )}
            {relativeDate !== '' && (
              <Text style={[styles.date, { color: colors.onSurfaceVariant }]}>{'\u00B7 ' + relativeDate}</Text>
            )}
          </View>
        </View>
        <TouchableOpacity style={styles.iconBtn} onPress={() => setShowMenu(true)}>
          <MaterialCommunityIcons name="dots-horizontal" size={22} color={colors.onSurfaceVariant} />
        </TouchableOpacity>
      </View>

      {/* Menu modal */}
      <Modal visible={showMenu} transparent animationType="fade" onRequestClose={() => setShowMenu(false)}>
        <Pressable style={styles.menuOverlay} onPress={() => setShowMenu(false)}>
          <Pressable onPress={(e) => e.stopPropagation()} style={[styles.menuCard, { backgroundColor: colors.surfaceContainerHighest, borderColor: colors.outlineVariant }]}>
            {onUnarchive && (
              <TouchableOpacity style={styles.menuItem} onPress={() => { setShowMenu(false); onUnarchive(); }}>
                <MaterialCommunityIcons name="archive-arrow-up-outline" size={18} color={colors.onSurface} />
                <Text style={[styles.menuText, { color: colors.onSurface }]}>{t('social.menu.unarchive')}</Text>
              </TouchableOpacity>
            )}
            {isOwn && onDelete && (
              <TouchableOpacity style={styles.menuItem} onPress={() => { setShowMenu(false); onDelete(); }}>
                <MaterialCommunityIcons name="delete-outline" size={18} color={colors.error} />
                <Text style={[styles.menuText, { color: colors.error }]}>{t('common.actions.delete')}</Text>
              </TouchableOpacity>
            )}
            <TouchableOpacity style={styles.menuItem} onPress={() => { setShowMenu(false); onPress?.(); }}>
              <MaterialCommunityIcons name="open-in-new" size={18} color={colors.onSurface} />
              <Text style={[styles.menuText, { color: colors.onSurface }]}>{t('social.menu.viewPost')}</Text>
            </TouchableOpacity>
            {onSaveToCollection && (
              <TouchableOpacity style={styles.menuItem} onPress={() => { setShowMenu(false); onSaveToCollection(); }}>
                <MaterialCommunityIcons name="folder-plus-outline" size={18} color={colors.onSurface} />
                <Text style={[styles.menuText, { color: colors.onSurface }]}>{t('social.menu.addToCollection')}</Text>
              </TouchableOpacity>
            )}
            {isOwn && onArchive && (
              <TouchableOpacity style={styles.menuItem} onPress={() => { setShowMenu(false); onArchive(); }}>
                <MaterialCommunityIcons name="archive-arrow-down-outline" size={18} color={colors.onSurface} />
                <Text style={[styles.menuText, { color: colors.onSurface }]}>{t('social.menu.archive')}</Text>
              </TouchableOpacity>
            )}
            {isOwn && onPin && (
              <TouchableOpacity style={styles.menuItem} onPress={() => { setShowMenu(false); onPin(); }}>
                <MaterialCommunityIcons name={post.isPinned ? 'pin-off-outline' : 'pin-outline'} size={18} color={colors.onSurface} />
                <Text style={[styles.menuText, { color: colors.onSurface }]}>{post.isPinned ? t('social.menu.unpin') : t('social.menu.pin')}</Text>
              </TouchableOpacity>
            )}
            {onReport && (
              <TouchableOpacity style={styles.menuItem} onPress={() => { setShowMenu(false); onReport(); }}>
                <MaterialCommunityIcons name="flag-outline" size={18} color={colors.onSurface} />
                <Text style={[styles.menuText, { color: colors.onSurface }]}>{t('common.actions.report')}</Text>
              </TouchableOpacity>
            )}
          </Pressable>
        </Pressable>
      </Modal>

      {/* Text */}
      <TouchableOpacity onPress={onPress} activeOpacity={0.9}>
        <View style={styles.textSection}>
          {textWithoutTags ? <Text style={[styles.body, { color: colors.onSurface }]}>{textWithoutTags}</Text> : null}
          {hashtags.length > 0 && (
            <View style={styles.tagsRow}>
              {hashtags.map((tag, i) => <Text key={i} style={[styles.hashtag, { color: colors.primary }]}>{tag}</Text>)}
            </View>
          )}
        </View>

        {/* Media */}
        {post.media && post.media.length === 2 && post.postType === 'AVANT_APRES' ? (
          <View style={styles.beforeAfter}>
            <View style={styles.baHalf}>
              <Image source={{ uri: resolveMediaUrl(post.media[0].mediaUrl) }} style={styles.baImage} contentFit="cover" />
              <View style={[styles.baLabelContainer, { backgroundColor: colors.inverseSurface }]}><Text style={[styles.baLabel, { color: colors.inverseOnSurface }]}>{t('social.beforeAfter.before')}</Text></View>
            </View>
            <View style={styles.baHalf}>
              <Image source={{ uri: resolveMediaUrl(post.media[1].mediaUrl) }} style={styles.baImage} contentFit="cover" />
              <View style={[styles.baLabelContainer, { backgroundColor: colors.secondary }]}><Text style={[styles.baLabel, { color: colors.onSecondary }]}>{t('social.beforeAfter.after')}</Text></View>
            </View>
            <View style={styles.swapIcon}>
              <MaterialCommunityIcons name="swap-horizontal" size={22} color={colors.primary} />
            </View>
          </View>
        ) : post.imageUrl ? (
          <Image source={{ uri: resolveMediaUrl(post.imageUrl) }} style={styles.singleImage} contentFit="cover" />
        ) : null}
      </TouchableOpacity>

      {/* Engagement */}
      <View style={styles.engagement}>
        <TouchableOpacity style={styles.engageBtn} onPress={onLike}>
          <MaterialCommunityIcons name={post.isLikedByCurrentUser ? 'heart' : 'heart-outline'} size={22} color={post.isLikedByCurrentUser ? colors.secondary : colors.onSurfaceVariant} />
          <Text style={[styles.engageCount, { color: colors.onSurfaceVariant }]}>{post.likesCount || 0}</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.engageBtn} onPress={onComment}>
          <MaterialCommunityIcons name="comment-outline" size={22} color={colors.onSurfaceVariant} />
          <Text style={[styles.engageCount, { color: colors.onSurfaceVariant }]}>{post.commentsCount || 0}</Text>
        </TouchableOpacity>
        {/* B31 : partage externe natif — compteur masqué (sharesCount = partages internes,
            que ce bouton ne déclenche plus ; reviendra avec le repartage-profil) */}
        <TouchableOpacity style={styles.engageBtn} onPress={onShare}>
          <MaterialCommunityIcons name="share-outline" size={22} color={colors.onSurfaceVariant} />
        </TouchableOpacity>
        <View style={{ flex: 1 }} />
        <TouchableOpacity style={styles.iconBtn} onPress={onBookmark}>
          <MaterialCommunityIcons name={post.isFavoritedByCurrentUser ? 'bookmark' : 'bookmark-outline'} size={22} color={post.isFavoritedByCurrentUser ? colors.tertiary : colors.onSurfaceVariant} />
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  article: {
    borderTopWidth: 1,
    borderBottomWidth: 1,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    paddingTop: 14,
    paddingBottom: 12,
    paddingHorizontal: 16,
  },
  headerInfo: { flex: 1, minWidth: 0 },
  nameRow: { flexDirection: 'row', alignItems: 'center', gap: 5 },
  name: { fontFamily: 'Manrope-SemiBold', fontSize: 15, fontWeight: '600' },
  metaRow: { flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 1, overflow: 'hidden' },
  typeBadge: {
    paddingHorizontal: 8,
    paddingVertical: 1,
    borderRadius: 999,
  },
  typeText: { fontFamily: 'Manrope-Bold', fontSize: 11, fontWeight: '700' },
  date: { fontFamily: 'Manrope-Regular', fontSize: 12, flexShrink: 0 },
  salonTagBtn: { flexShrink: 1, minWidth: 0 },
  salonTag: { fontFamily: 'Manrope-SemiBold', fontSize: 12, fontWeight: '600' },
  byAuthor: { fontFamily: 'Manrope-Regular', fontSize: 12, flexShrink: 1 },
  iconBtn: { width: 44, height: 44, alignItems: 'center', justifyContent: 'center' },
  textSection: { paddingHorizontal: 16, paddingBottom: 12 },
  body: { fontFamily: 'Manrope-Regular', fontSize: 14, lineHeight: 21.7 },
  tagsRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginTop: 8 },
  hashtag: { fontFamily: 'Manrope-Bold', fontSize: 12, fontWeight: '700' },
  singleImage: { width: '100%', height: 300 },
  beforeAfter: { flexDirection: 'row', gap: 3, position: 'relative' },
  baHalf: { flex: 1, position: 'relative' },
  baImage: { width: '100%', height: 260 },
  baLabelContainer: {
    position: 'absolute',
    bottom: 12,
    start: 12,
    paddingVertical: 4,
    paddingHorizontal: 10,
    borderRadius: 999,
  },
  baLabel: {
    fontFamily: 'Manrope-Bold',
    fontSize: 10.5,
    fontWeight: '800',
    letterSpacing: 1.5,
  },
  swapIcon: {
    position: 'absolute',
    top: '50%',
    start: '50%',
    transform: [{ translateX: -20 }, { translateY: -20 }],
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: 'rgba(255,255,255,0.92)', // design-fixed
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: 'rgb(39,26,44)', // design-fixed
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
  },
  engagement: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingTop: 12,
    paddingBottom: 14,
    paddingHorizontal: 12,
    gap: 4,
  },
  engageBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 999,
  },
  engageCount: {
    fontFamily: 'Manrope-SemiBold',
    fontSize: 14,
    fontWeight: '600',
  },
  // Menu overlay + card (Modal-based, reliably above everything)
  menuOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.3)', // design-fixed
    justifyContent: 'flex-start',
    alignItems: 'flex-end',
    paddingTop: 180,
    paddingEnd: 16,
  },
  menuCard: {
    minWidth: 200,
    borderRadius: 16,
    borderWidth: 1,
    paddingVertical: 8,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    paddingVertical: 12,
    paddingHorizontal: 16,
  },
  menuText: { fontFamily: 'Manrope-SemiBold', fontSize: 14, fontWeight: '600' },
});
