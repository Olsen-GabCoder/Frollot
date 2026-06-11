import React, { useState } from 'react';
import { View, Text, TouchableOpacity, Pressable, Modal, StyleSheet } from 'react-native';
import { Image } from 'expo-image';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Avatar } from '../common/Avatar';
import { PostResponse } from '../../types';
import { useTheme } from '../../theme';
import { resolveMediaUrl } from '../../utils/media';

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
}

export function PostCard({
  post, currentUserId, onLike, onComment, onShare,
  onBookmark, onPress, onProfilePress, onDelete, onReport, onUnarchive, onSaveToCollection, onArchive,
}: PostCardProps) {
  const { colors } = useTheme();
  const [showMenu, setShowMenu] = useState(false);
  const hashtags = post.content?.match(/#\w+/g) || [];
  const textWithoutTags = post.content?.replace(/#\w+/g, '').trim();
  const isOwn = currentUserId && post.authorId === currentUserId;

  return (
    <View style={[styles.article, { backgroundColor: colors.surface, borderColor: colors.outlineVariant }]}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={onProfilePress}>
          <Avatar initials={post.authorName?.[0] || 'U'} size={44} ring tone="primary" imageUrl={post.authorAvatarUrl} />
        </TouchableOpacity>
        <View style={styles.headerInfo}>
          <View style={styles.nameRow}>
            <Text style={[styles.name, { color: colors.onSurface }]} numberOfLines={1}>{post.authorName}</Text>
            <MaterialCommunityIcons name="check-decagram" size={16} color={colors.primary} />
          </View>
          <View style={styles.metaRow}>
            {post.authorUserType && (
              <View style={[styles.typeBadge, { backgroundColor: colors.secondaryContainer }]}>
                <Text style={[styles.typeText, { color: colors.secondary }]}>{post.authorUserType === 'salon_owner' ? 'Salon' : 'Coiffeur'}</Text>
              </View>
            )}
            <Text style={[styles.date, { color: colors.onSurfaceVariant }]}>· {post.createdAt || ''}</Text>
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
                <Text style={[styles.menuText, { color: colors.onSurface }]}>Désarchiver</Text>
              </TouchableOpacity>
            )}
            {isOwn && onDelete && (
              <TouchableOpacity style={styles.menuItem} onPress={() => { setShowMenu(false); onDelete(); }}>
                <MaterialCommunityIcons name="delete-outline" size={18} color={colors.error} />
                <Text style={[styles.menuText, { color: colors.error }]}>Supprimer</Text>
              </TouchableOpacity>
            )}
            <TouchableOpacity style={styles.menuItem} onPress={() => { setShowMenu(false); onPress?.(); }}>
              <MaterialCommunityIcons name="open-in-new" size={18} color={colors.onSurface} />
              <Text style={[styles.menuText, { color: colors.onSurface }]}>Voir le post</Text>
            </TouchableOpacity>
            {onSaveToCollection && (
              <TouchableOpacity style={styles.menuItem} onPress={() => { setShowMenu(false); onSaveToCollection(); }}>
                <MaterialCommunityIcons name="folder-plus-outline" size={18} color={colors.onSurface} />
                <Text style={[styles.menuText, { color: colors.onSurface }]}>Ajouter à une collection</Text>
              </TouchableOpacity>
            )}
            {isOwn && onArchive && (
              <TouchableOpacity style={styles.menuItem} onPress={() => { setShowMenu(false); onArchive(); }}>
                <MaterialCommunityIcons name="archive-arrow-down-outline" size={18} color={colors.onSurface} />
                <Text style={[styles.menuText, { color: colors.onSurface }]}>Archiver</Text>
              </TouchableOpacity>
            )}
            {onReport && (
              <TouchableOpacity style={styles.menuItem} onPress={() => { setShowMenu(false); onReport(); }}>
                <MaterialCommunityIcons name="flag-outline" size={18} color={colors.onSurface} />
                <Text style={[styles.menuText, { color: colors.onSurface }]}>Signaler</Text>
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
              <View style={[styles.baLabelContainer, { backgroundColor: colors.inverseSurface }]}><Text style={[styles.baLabel, { color: colors.inverseOnSurface }]}>AVANT</Text></View>
            </View>
            <View style={styles.baHalf}>
              <Image source={{ uri: resolveMediaUrl(post.media[1].mediaUrl) }} style={styles.baImage} contentFit="cover" />
              <View style={[styles.baLabelContainer, { backgroundColor: colors.secondary }]}><Text style={[styles.baLabel, { color: colors.onSecondary }]}>APRES</Text></View>
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
  metaRow: { flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 1 },
  typeBadge: {
    paddingHorizontal: 8,
    paddingVertical: 1,
    borderRadius: 999,
  },
  typeText: { fontFamily: 'Manrope-Bold', fontSize: 11, fontWeight: '700' },
  date: { fontFamily: 'Manrope-Regular', fontSize: 12 },
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
    left: 12,
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
    left: '50%',
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
    paddingRight: 16,
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
