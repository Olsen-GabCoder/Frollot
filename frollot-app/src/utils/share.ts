import { Share } from 'react-native';
import { PostResponse } from '../types';

// B31 : partage externe natif (menu système : WhatsApp, SMS, mail, copier…).
// Pas d'URL publique de post aujourd'hui (aucune page web publique ni deep link
// configuré) — on ne fabrique pas de faux lien : texte seul. Le jour où une URL
// publique existera, c'est ici (et uniquement ici) qu'on l'ajoutera au payload.
export async function sharePostExternally(post: PostResponse): Promise<void> {
  const content = (post.content || '').trim();
  const excerpt = content.length > 200 ? `${content.slice(0, 200)}…` : content;
  const message = excerpt
    ? `« ${excerpt} »\n\n— ${post.authorName} sur Frollot`
    : `Découvrez cette publication de ${post.authorName} sur Frollot`;
  await Share.share({ message, title: `Publication de ${post.authorName}` });
}

// Annulation utilisateur = pas une erreur (sur web, navigator.share rejette en
// AbortError quand l'utilisateur ferme le menu ; sur natif, l'annulation résout
// sans rejeter). Seul un vrai échec doit produire un retour visible.
export function isShareCancellation(error: unknown): boolean {
  return error instanceof Error && error.name === 'AbortError';
}
